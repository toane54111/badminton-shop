package com.badmintonshop.service;

import com.badmintonshop.dto.inventory.InventoryAdjustRequest;
import com.badmintonshop.dto.inventory.InventoryDTO;
import com.badmintonshop.dto.inventory.InventoryTransactionDTO;
import com.badmintonshop.entity.Inventory;
import com.badmintonshop.entity.InventoryTransaction;
import com.badmintonshop.entity.enums.TransactionType;
import com.badmintonshop.repository.InventoryRepository;
import com.badmintonshop.repository.InventoryTransactionRepository;
import com.badmintonshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Inventory operations with atomic operations and race condition
 * handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryService {

        private final InventoryRepository inventoryRepository;
        private final InventoryTransactionRepository transactionRepository;
        private final ProductRepository productRepository;

        /**
         * Get all inventory with pagination
         */
        public Page<InventoryDTO> getAllInventory(Pageable pageable) {
                return inventoryRepository.findAll(pageable)
                                .map(InventoryDTO::fromEntity);
        }

        /**
         * Search inventory with filters
         */
        public Page<InventoryDTO> searchInventory(Long productId, Boolean lowStock, Pageable pageable) {
                return inventoryRepository.searchInventory(productId, lowStock, pageable)
                                .map(InventoryDTO::fromEntity);
        }

        /**
         * Get inventory by product ID
         */
        public List<InventoryDTO> getInventoryByProductId(Long productId) {
                return inventoryRepository.findByProductProductId(productId).stream()
                                .map(InventoryDTO::fromEntity)
                                .collect(Collectors.toList());
        }

        /**
         * Get low stock inventory
         */
        public List<InventoryDTO> getLowStockInventory() {
                return inventoryRepository.findLowStock().stream()
                                .map(InventoryDTO::fromEntity)
                                .collect(Collectors.toList());
        }

        /**
         * Get low stock inventory with pagination
         */
        public Page<InventoryDTO> getLowStockInventory(Pageable pageable) {
                return inventoryRepository.findLowStock(pageable)
                                .map(InventoryDTO::fromEntity);
        }

        /**
         * Update stock quantity (set absolute value)
         */
        @Transactional
        public InventoryDTO updateStock(Long inventoryId, int newQuantity, String reason) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                int previousQuantity = inventory.getQuantityAvailable();
                int adjustment = newQuantity - previousQuantity;

                inventory.setQuantityAvailable(newQuantity);
                inventory.setUpdatedAt(LocalDateTime.now());
                inventory = inventoryRepository.save(inventory);

                // Record transaction - use IN for increase, OUT for decrease
                createTransaction(inventory, previousQuantity, newQuantity, Math.abs(adjustment),
                                adjustment > 0 ? TransactionType.IN : TransactionType.OUT,
                                reason, "adjustment", null);

                log.info("Updated inventory {}: {} -> {}", inventoryId, previousQuantity, newQuantity);
                return InventoryDTO.fromEntity(inventory);
        }

        /**
         * Adjust stock quantity (atomic operation with race condition handling)
         */
        @Transactional
        public InventoryDTO adjustStock(InventoryAdjustRequest request) {
                Long inventoryId = request.getInventoryId();
                int adjustment = request.getAdjustment();

                if (inventoryId == null) {
                        throw new IllegalArgumentException("ID tồn kho không được để trống");
                }

                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                int previousQuantity = inventory.getQuantityAvailable();

                // Use atomic update to handle race conditions
                int rowsAffected = inventoryRepository.adjustQuantity(inventoryId, adjustment);

                if (rowsAffected == 0) {
                        throw new IllegalArgumentException("Không thể điều chỉnh tồn kho. Số lượng không đủ.");
                }

                // Refresh entity
                inventory = inventoryRepository.findById(inventoryId).get();
                int newQuantity = inventory.getQuantityAvailable();

                // Record transaction - use ADJUSTMENT for manual adjustments
                createTransaction(inventory, previousQuantity, newQuantity, Math.abs(adjustment),
                                TransactionType.ADJUSTMENT,
                                request.getReason(), "adjustment", null);

                log.info("Adjusted inventory {}: {} units", inventoryId, adjustment);
                return InventoryDTO.fromEntity(inventory);
        }

        /**
         * Reserve stock for an order
         */
        @Transactional
        public void reserveStock(Long inventoryId, int quantity) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                if (inventory.getActualAvailable() < quantity) {
                        throw new IllegalArgumentException("Không đủ hàng trong kho");
                }

                int previousQuantity = inventory.getQuantityAvailable();
                inventory.reserve(quantity);
                inventoryRepository.save(inventory);

                // Use OUT for reservation (stock going out)
                createTransaction(inventory, previousQuantity, inventory.getQuantityAvailable(), quantity,
                                TransactionType.OUT, "Đặt hàng - Reserved", "order", null);
                log.info("Reserved {} units from inventory {}", quantity, inventoryId);
        }

        /**
         * Release reserved stock
         */
        @Transactional
        public void releaseReservation(Long inventoryId, int quantity) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                int previousQuantity = inventory.getQuantityAvailable();
                inventory.releaseReservation(quantity);
                inventoryRepository.save(inventory);

                // Use RETURN for releasing reservation (stock returning)
                createTransaction(inventory, previousQuantity, inventory.getQuantityAvailable(), quantity,
                                TransactionType.RETURN, "Hủy đặt hàng - Released", "order", null);
                log.info("Released {} units reservation from inventory {}", quantity, inventoryId);
        }

        /**
         * Complete sale (reduce from reserved)
         */
        @Transactional
        public void completeSale(Long inventoryId, int quantity, Long orderId) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                int previousQuantity = inventory.getQuantityAvailable();
                inventory.sell(quantity);
                inventoryRepository.save(inventory);

                // Use OUT for completed sale
                createTransaction(inventory, previousQuantity, inventory.getQuantityAvailable(), quantity,
                                TransactionType.OUT, "Bán hàng", "order", orderId);
                log.info("Completed sale of {} units from inventory {}", quantity, inventoryId);
        }

        /**
         * Restock inventory
         */
        @Transactional
        public InventoryDTO restock(Long inventoryId, int quantity, Long purchaseOrderId) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy tồn kho: " + inventoryId));

                int previousQuantity = inventory.getQuantityAvailable();
                inventory.restock(quantity);
                inventoryRepository.save(inventory);

                // Use IN for restocking
                createTransaction(inventory, previousQuantity, inventory.getQuantityAvailable(), quantity,
                                TransactionType.IN, "Nhập kho", "purchase_order", purchaseOrderId);
                log.info("Restocked {} units to inventory {}", quantity, inventoryId);
                return InventoryDTO.fromEntity(inventory);
        }

        /**
         * Get inventory statistics
         */
        public InventoryStats getInventoryStats() {
                return InventoryStats.builder()
                                .totalItems(inventoryRepository.count())
                                .lowStockItems(inventoryRepository.countLowStock())
                                .outOfStockItems(inventoryRepository.countOutOfStock())
                                .build();
        }

        /**
         * Create inventory transaction record
         */
        private void createTransaction(Inventory inventory, int quantityBefore, int quantityAfter,
                        int quantity, TransactionType type, String reason, String referenceType, Long referenceId) {
                InventoryTransaction transaction = InventoryTransaction.builder()
                                .product(inventory.getProduct())
                                .variant(inventory.getVariant())
                                .transactionType(type)
                                .quantity(quantity)
                                .quantityBefore(quantityBefore)
                                .quantityAfter(quantityAfter)
                                .reason(reason)
                                .referenceType(referenceType)
                                .referenceId(referenceId)
                                .createdAt(LocalDateTime.now())
                                .build();
                transactionRepository.save(transaction);
        }

        /**
         * Get transaction history with filters
         */
        public Page<InventoryTransactionDTO> getTransactionHistory(
                        Long productId,
                        TransactionType transactionType,
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Pageable pageable) {
                return transactionRepository
                                .searchTransactions(productId, transactionType, startDate, endDate, pageable)
                                .map(InventoryTransactionDTO::fromEntity);
        }

        @lombok.Builder
        @lombok.Getter
        public static class InventoryStats {
                private long totalItems;
                private long lowStockItems;
                private long outOfStockItems;
        }
}
