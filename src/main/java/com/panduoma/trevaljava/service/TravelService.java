package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.utils.LLMutils;
import com.panduoma.trevaljava.vo.StreamChunkVO;
import com.panduoma.trevaljava.vo.StreamDoneVO;
import com.panduoma.trevaljava.vo.StreamErrorVO;
import com.panduoma.trevaljava.vo.TravelRecommendVO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.function.Consumer;

import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.entity.User;
import jakarta.annotation.Resource;
import java.util.List;

@Service // 标记为Spring的服务层Bean，会被自动扫描并管理
public class TravelService {
    @Value("${llm.api-key}")
    private String apiKey;
    @Value("${llm.base-url}")
    private String baseUrl;
    @Value("${llm.model}")
    private String model;
    private LLMutils llmutils;// 大模型工具类实例
    private ObjectMapper objectMapper = new ObjectMapper();// JSON序列化/反序列化工具

    @Resource
    private UserMapper userMapper;
    // ============================================================
    // 3. 初始化方法（Bean创建后自动执行）
    // ============================================================

    @PostConstruct // 在依赖注入完成后执行
    public void init() {
        this.llmutils = new LLMutils(apiKey, baseUrl, model);
        // System.out.println("llmutils "+llmutils);
    }

    // ============================================================
    // 4. 旅游推荐方法（供Controller调用）
    // ============================================================
    public TravelRecommendVO recommend(String city, Integer days, Double budget) {
        TravelRecommendVO result = new TravelRecommendVO();

        // 构建提示词（把用户需求转成给AI的指令）
        String prompt = buildTravelPrompt(city, budget, days);

        try {
            // 调用大模型API（Chat方法，非流式）
            String response = llmutils.Chat(null, prompt);
            // 解析AI返回的JSON数据
            return parseTravelResponse(response);
        } catch (Exception e) {
            // 发生异常时，设置失败状态
            result.setSuccess(false);
            result.setError("旅游推荐失败");
            return result;
        }
    }

    // ============================================================
    // 5. 解析AI返回数据（私有辅助方法）
    // ============================================================
    // service旅游推荐返回数据处理
    private TravelRecommendVO parseTravelResponse(String response) {
        TravelRecommendVO result = new TravelRecommendVO();
        try {
            String jsonContent = extractJson(response);
            if (jsonContent != null) {
                System.out.println("=== 准备解析的 JSON ===");
                System.out.println(jsonContent);
                result = objectMapper.readValue(jsonContent, TravelRecommendVO.class);
                result.setSuccess(true);
                System.out.println("=== 解析成功 ===");
            } else {
                System.out.println("=== extractJson 返回 null ===");
                result.setSuccess(false);
                result.setError("无法从响应中提取JSON");
                result.setResponse(response);
            }
        } catch (Exception e) {
            System.out.println("=== JSON 解析异常: " + e.getMessage() + " ===");
            e.printStackTrace();
            result.setSuccess(false);
            result.setError("JSON解析失败: " + e.getMessage());
            result.setRawResponse(response);
        }
        return result;
    }

    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        System.out.println("=== AI 原始响应 ===");
        System.out.println(response);
        System.out.println("=== 响应长度: " + response.length() + " ===");

        String[] patterns = {
                "```json\\n([\\s\\S]*?)\\n```",
                "```\\n([\\s\\S]*?)\\n```"
        };
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(response);
            if (m.find()) {
                String extracted = m.group(1);
                System.out.println("=== 正则提取到 JSON 长度: " + extracted.length() + " ===");
                return extracted;
            }
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            String extracted = response.substring(start, end + 1);
            System.out.println("=== 大括号提取到 JSON 长度: " + extracted.length() + " ===");
            return extracted;
        }
        return null;
    }

    private String buildTravelPrompt(String city, Double budget, Integer days) {
        return "你是一个专业的旅游规划师。请为游客制定一份详细的" + city +
                days + "日游计划，预算为" + budget + "元。\n\n" +
                "请严格按照以下 JSON 格式返回结果，不要添加任何额外的文字说明：\n" +
                "```json\n" +
                "{\n" +
                "  \"city\": \"" + city + "\",\n" +
                "  \"days\": " + days + ",\n" +
                "  \"totalBudget\": " + budget + ",\n" +
                "  \"dailyItinerary\": [\n" +
                "    {\n" +
                "      \"day\": 1,\n" +
                "      \"date\": \"第一天\",\n" +
                "      \"morning\": [{\"time\": \"09:00\", \"activity\": \"景点名称和描述\"}],\n" +
                "      \"afternoon\": [{\"time\": \"14:00\", \"activity\": \"景点名称和描述\"}],\n" +
                "      \"evening\": [{\"time\": \"19:00\", \"activity\": \"活动名称\"}]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"budgetBreakdown\": {\n" +
                "    \"accommodation\": 0,\n" +
                "    \"food\": 0,\n" +
                "    \"transportation\": 0,\n" +
                "    \"tickets\": 0,\n" +
                "    \"other\": 0\n" +
                "  },\n" +
                "  \"tips\": [\"注意事项1\", \"注意事项2\"],\n" +
                "  \"warnings\": [\"安全提示1\"]\n" +
                "}\n" +
                "```";
    }

    public SseEmitter chat(String message) throws IOException {
        SseEmitter emitter = new SseEmitter(180000L);
        // 发送的处理逻辑
        new Thread(() -> {
            try {
                String systemPrompt = "你是一个很好的旅游助手，请用中文回答用户关于旅游的问题";
                Consumer<String> callback = content -> {
                    try {
                        String json = objectMapper.writeValueAsString(StreamChunkVO.of(content));
                        emitter.send(SseEmitter.event().data(json));
                    } catch (Exception e) {
                        System.out.println("消息发送失败" + e);
                    }
                };
                llmutils.chatStream(systemPrompt, message, callback);
                String doneJson = objectMapper.writeValueAsString(StreamDoneVO.of());
                emitter.send(SseEmitter.event().data(doneJson));
                emitter.complete();
            } catch (Exception e) {
                try {
                    String errorJson = objectMapper.writeValueAsString(StreamErrorVO.of(e.getMessage()));

                    emitter.send(SseEmitter.event().data(errorJson));
                } catch (Exception e1) {
                    System.out.println("消息发送错误" + e);
                }
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    /**
     * 查询所有正常、未删除的用户
     * 
     * @return 用户列表
     */
    public List<User> getAllValidUser() {
        // 调用Mapper中定义的查询方法
        return userMapper.selectAllValidUser();
    }

}
