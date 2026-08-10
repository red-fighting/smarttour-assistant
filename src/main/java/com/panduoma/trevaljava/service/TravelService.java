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

@Service// 标记为Spring的服务层Bean，会被自动扫描并管理
public class TravelService {
    @Value("${llm.api-key}")
    private String apiKey;
    @Value("${llm.base-url}")
    private String baseUrl;
    @Value("${llm.model}")
    private String model;
    private  LLMutils llmutils;// 大模型工具类实例
    private  ObjectMapper objectMapper=new ObjectMapper();// JSON序列化/反序列化工具

    @Resource
    private UserMapper userMapper;
    // ============================================================
    // 3. 初始化方法（Bean创建后自动执行）
    // ============================================================

    @PostConstruct // 在依赖注入完成后执行
    public void init() {
        this.llmutils = new LLMutils(apiKey, baseUrl, model);
        //System.out.println("llmutils "+llmutils);
    }


    // ============================================================
    // 4. 旅游推荐方法（供Controller调用）
    // ============================================================
    public TravelRecommendVO recommend(String city, Integer days, Double budget){
        TravelRecommendVO result = new TravelRecommendVO();

        // 构建提示词（把用户需求转成给AI的指令）
        String prompt=buildTravelPrompt(city,budget,days);

        try{
            // 调用大模型API（Chat方法，非流式）
           String response=llmutils.Chat(null,prompt);
            // 解析AI返回的JSON数据
            return parseTravelResponse(response);
        }catch (Exception e){
            // 发生异常时，设置失败状态
            result.setSuccess(false);
            result.setError("旅游推荐失败");
            return result;
        }
    }
    // ============================================================
    // 5. 解析AI返回数据（私有辅助方法）
    // ============================================================
    //service旅游推荐返回数据处理
    private TravelRecommendVO parseTravelResponse(String response){
        TravelRecommendVO result=new TravelRecommendVO();
        try{
            String jsonContent=extractJson(response);
            if(jsonContent!=null){
                result=objectMapper.readValue(jsonContent, TravelRecommendVO.class);
                result.setSuccess(true);
            }else {
                result.setSuccess(false);
                result.setError("");
                result.setResponse(response);
            }
        }catch (Exception e){
            result.setSuccess(false);
            result.setError("未能从响应中提取JSON");
            result.setRawResponse(response);
        }
        return result;
    }
    private String extractJson(String response){
        if(response==null||response.isEmpty()){
            return null;
        }
        String[] patterns={
                "```json\\n([\\s\\S]*?)\\n```",
                "```\\n([\\s\\S]*?)\\n```"
        };
        for(String pattern:patterns){
            java.util.regex.Pattern p=java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m=p.matcher(response);
            if(m.find()){
                return m.group(1);
            }
        }
        int start=response.indexOf('{');
        int end=response.indexOf('}');
        if(start!=-1&&end!=-1&&start<end){
            return response.substring(start,end+1);
        }
        return null;
    }



    private String buildTravelPrompt(String city, Double budget, Integer days) {
        return "你是一个专业的旅游规划师。请为游客制定一份详细的" + city +
                days + "日游计划，预算为" + budget + "元。\n\n" +
                "请提供：\n" +
                "1. 每日行程安排（上午/下午/晚上）\n" +
                "2. 每个景点的介绍和门票价格\n" +
                "3. 交通建议\n" +
                "4. 详细的预算分配表\n" +
                "5. 旅行注意事项\n\n" +
                "请以JSON格式返回结果，包含city、days、totalBudget、dailyItinerary、budgetBreakdown、tips等字段。";
    }
    public SseEmitter chat(String message)throws IOException {
        SseEmitter emitter=new SseEmitter(180000L);
        //发送的处理逻辑
        new Thread(()->{
            try{
                String systemPrompt="你是一个很好的旅游助手，请用中文回答用户关于旅游的问题";
                Consumer<String> callback = content-> {
                    try{
                        String json=objectMapper.writeValueAsString(StreamChunkVO.of(content));
                        emitter.send(SseEmitter.event().data(json));
                    }catch (Exception e){
                        System.out.println("消息发送失败"+e);
                    }
                };
                llmutils.chatStream(systemPrompt,message,callback);
                String doneJson=objectMapper.writeValueAsString(StreamDoneVO.of());
                emitter.send(SseEmitter.event().data(doneJson));
                emitter.complete();
            }catch (Exception e){
                try {
                    String errorJson=objectMapper.writeValueAsString(StreamErrorVO.of(e.getMessage()));

                    emitter.send(SseEmitter.event().data(errorJson));
                }catch (Exception e1){
                    System.out.println("消息发送错误"+e);
                }
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
    /**
     * 查询所有正常、未删除的用户
     * @return 用户列表
     */
    public List<User> getAllValidUser() {
        // 调用Mapper中定义的查询方法
        return userMapper.selectAllValidUser();
    }

}
