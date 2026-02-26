package com.badmintonshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để gọi OpenRouter AI API
 * API key được lưu trong application-secrets.properties để bảo mật
 * Knowledge base được load động từ file và database
 */
@Service
@Slf4j
public class ChatbotService {

    @Value("${openrouter.api.key:}")
    private String apiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    @Value("${openrouter.model:alibaba/tongyi-deepresearch-30b-a3b:free}")
    private String model;

    private final RestTemplate restTemplate;
    
    // Inject các service để lấy dữ liệu động
    private final ProductService productService;
    private final PromotionService promotionService;
    private final SystemSettingService systemSettingService;

    public ChatbotService(ProductService productService, 
                          PromotionService promotionService,
                          SystemSettingService systemSettingService) {
        this.restTemplate = new RestTemplate();
        this.productService = productService;
        this.promotionService = promotionService;
        this.systemSettingService = systemSettingService;
    }

    /**
     * Load knowledge base từ file chatbot-knowledge.md
     */
    private String loadKnowledgeFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/chatbot-knowledge.md");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.warn("Could not load chatbot-knowledge.md: {}", e.getMessage());
            return getDefaultKnowledge();
        }
    }

    /**
     * Default knowledge khi không load được từ file
     */
    private String getDefaultKnowledge() {
        return """
                # Thông tin cửa hàng Badminton Shop

                ## Giới thiệu
                Badminton Shop là cửa hàng chuyên cung cấp các sản phẩm cầu lông chính hãng.

                ## Thông tin liên hệ
                - Hotline: 1900 1234
                - Email: support@badmintonshop.com
                - Thời gian làm việc: 8:00 - 22:00 hàng ngày

                ## Chính sách giao hàng
                - Nội thành TP.HCM và Hà Nội: 1-2 ngày
                - Các tỉnh khác: 2-5 ngày
                - Miễn phí ship cho đơn từ 500.000đ

                ## Chính sách đổi trả
                - Đổi trả miễn phí trong 7 ngày nếu lỗi nhà sản xuất
                - Sản phẩm còn nguyên tem, chưa qua sử dụng

                ## Bảo hành
                - Vợt cầu lông: 6-12 tháng
                - Giày: 3 tháng
                - Túi vợt: 6 tháng
                """;
    }

    /**
     * Tạo knowledge base động từ dữ liệu hiện tại của website
     * Được gọi mỗi lần có câu hỏi để đảm bảo dữ liệu mới nhất
     */
    private String buildDynamicKnowledge() {
        StringBuilder knowledge = new StringBuilder();
        
        // 1. Load từ file cơ bản
        String fileKnowledge = loadKnowledgeFromFile();
        knowledge.append(fileKnowledge);
        knowledge.append("\n\n");
        
        // 2. Thêm thông tin khuyến mãi hiện tại
        try {
            var activePromotions = promotionService.getActivePromotions();
            if (activePromotions != null && !activePromotions.isEmpty()) {
                knowledge.append("## Khuyến mãi đang diễn ra\n");
                int count = 0;
                for (var promo : activePromotions) {
                    if (count >= 5) break; // Giới hạn 5 khuyến mãi
                    knowledge.append("- ").append(promo.getName());
                    if (promo.getDiscountValue() != null) {
                        knowledge.append(": Giảm ").append(promo.getDiscountValue());
                        if (promo.getDiscountType() != null && promo.getDiscountType().name().equals("PERCENTAGE")) {
                            knowledge.append("%");
                        } else {
                            knowledge.append("đ");
                        }
                    }
                    knowledge.append("\n");
                    count++;
                }
                knowledge.append("\n");
            }
        } catch (Exception e) {
            log.debug("Could not load promotions for chatbot: {}", e.getMessage());
        }

        // 3. Thêm thông tin từ System Settings
        try {
            String hotline = systemSettingService.getValue("contact_hotline", "1900 1234");
            String email = systemSettingService.getValue("contact_email", "support@badmintonshop.com");
            String address = systemSettingService.getValue("contact_address", "");
            String freeShip = systemSettingService.getValue("free_shipping_threshold", "500000");
            
            knowledge.append("## Thông tin liên hệ (cập nhật)\n");
            knowledge.append("- Hotline: ").append(hotline).append("\n");
            knowledge.append("- Email: ").append(email).append("\n");
            if (!address.isEmpty()) {
                knowledge.append("- Địa chỉ: ").append(address).append("\n");
            }
            knowledge.append("- Miễn phí ship cho đơn từ: ").append(freeShip).append("đ\n");
            knowledge.append("\n");
        } catch (Exception e) {
            log.debug("Could not load system settings for chatbot: {}", e.getMessage());
        }

        // 4. Thêm top sản phẩm bán chạy
        try {
            var topProducts = productService.getBestSellers(5);
            if (topProducts != null && !topProducts.isEmpty()) {
                knowledge.append("## Sản phẩm bán chạy nhất\n");
                for (var product : topProducts) {
                    knowledge.append("- ").append(product.getName());
                    if (product.getBasePrice() != null) {
                        knowledge.append(" - Giá: ").append(String.format("%,.0f", product.getBasePrice())).append("đ");
                    }
                    knowledge.append("\n");
                }
                knowledge.append("\n");
            }
        } catch (Exception e) {
            log.debug("Could not load top products for chatbot: {}", e.getMessage());
        }

        // 5. Thêm sản phẩm mới
        try {
            var newProducts = productService.getNewArrivals(5);
            if (newProducts != null && !newProducts.isEmpty()) {
                knowledge.append("## Sản phẩm mới nhất\n");
                for (var product : newProducts) {
                    knowledge.append("- ").append(product.getName());
                    if (product.getBasePrice() != null) {
                        knowledge.append(" - Giá: ").append(String.format("%,.0f", product.getBasePrice())).append("đ");
                    }
                    knowledge.append("\n");
                }
                knowledge.append("\n");
            }
        } catch (Exception e) {
            log.debug("Could not load new products for chatbot: {}", e.getMessage());
        }

        return knowledge.toString();
    }

    /**
     * Check if API is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY");
    }

    /**
     * Gọi OpenRouter API để lấy response từ AI
     * Knowledge được load mới mỗi lần gọi để đảm bảo dữ liệu cập nhật
     */
    public String chat(String userMessage) {
        if (!isConfigured()) {
            log.warn("OpenRouter API key not configured");
            return getFallbackResponse(userMessage);
        }

        try {
            // Load knowledge động mỗi lần chat
            String currentKnowledge = buildDynamicKnowledge();
            log.info("Loaded knowledge base with {} characters", currentKnowledge.length());

            // Build system prompt với knowledge mới nhất
            String systemPrompt = String.format("""
                    Bạn là trợ lý AI thân thiện của Badminton Shop - cửa hàng cầu lông chính hãng.

                    THÔNG TIN CỬA HÀNG VÀ WEBSITE (CẬP NHẬT MỚI NHẤT):
                    %s

                    HƯỚNG DẪN TRẢ LỜI:
                    - Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp
                    - Sử dụng emoji phù hợp để tạo sự thân thiện 🏸
                    - Nếu có khuyến mãi hoặc sản phẩm mới, hãy giới thiệu cho khách
                    - Nếu không biết câu trả lời, hướng dẫn liên hệ hotline
                    - Trả lời ngắn gọn, dễ hiểu (tối đa 200 từ)
                    - Sử dụng thông tin mới nhất từ dữ liệu được cung cấp ở trên
                    """, currentKnowledge);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.7);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));
            requestBody.put("messages", messages);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://badmintonshop.com");
            headers.set("X-Title", "Badminton Shop Chatbot");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling OpenRouter API (model: {}) for message: {}",
                    model, userMessage.substring(0, Math.min(50, userMessage.length())));

            // Call API
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            // Parse response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        log.info("OpenRouter API response received successfully");
                        return content;
                    }
                }
            }

            log.warn("Invalid response from OpenRouter API");
            return getFallbackResponse(userMessage);

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage());
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * Fallback response khi API không hoạt động
     */
    private String getFallbackResponse(String message) {
        String lowerMessage = message.toLowerCase();

        Map<String, String> responses = Map.ofEntries(
                Map.entry("đặt hàng",
                        "Để đặt hàng, bạn có thể:\n• Đặt hàng online 24/7 qua website\n• Thanh toán: COD, chuyển khoản, Momo, ZaloPay 📱"),
                Map.entry("giao hàng",
                        "Chính sách giao hàng:\n• Nội thành: 1-2 ngày\n• Tỉnh khác: 2-5 ngày\n• Miễn phí ship đơn từ 500.000đ 🚚"),
                Map.entry("ship", "Phí ship:\n• Miễn phí cho đơn từ 500.000đ\n• Đơn dưới 500.000đ: phí 30.000đ 🚀"),
                Map.entry("đổi trả", "Đổi trả miễn phí trong 7 ngày nếu lỗi nhà sản xuất ✅"),
                Map.entry("bảo hành", "Bảo hành:\n• Vợt: 6-12 tháng\n• Giày: 3 tháng\n• Túi: 6 tháng 🛡️"),
                Map.entry("liên hệ", "Hotline: 1900 1234\nEmail: support@badmintonshop.com 📞"),
                Map.entry("xin chào", "Xin chào! 👋 Tôi có thể giúp gì cho bạn?"),
                Map.entry("hello", "Xin chào! 👋 Tôi là trợ lý AI của Badminton Shop."),
                Map.entry("khuyến mãi", "Hãy truy cập trang chủ để xem các khuyến mãi mới nhất! 🎁"),
                Map.entry("vợt", "Chúng tôi có vợt Yonex, Victor, Li-Ning chính hãng. Xem tại mục Sản phẩm! 🏸"));

        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Cảm ơn bạn đã liên hệ! 😊\n\nĐể được hỗ trợ nhanh nhất:\n• Hotline: 1900 1234\n• Email: support@badmintonshop.com";
    }
}
