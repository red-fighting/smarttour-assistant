package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.dto.ChatRequestDTO;
import com.panduoma.trevaljava.dto.TravelRequestDTO;
import com.panduoma.trevaljava.service.TravelService;
import com.panduoma.trevaljava.vo.Result;
import com.panduoma.trevaljava.vo.TravelRecommendVO;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.panduoma.trevaljava.entity.User;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Data
@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor//不想写私有函数的构造可以直接用
public class TravelController {
    @Resource
    private final TravelService travelService;


    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello");
    }
    // 新增用户列表接口
    @GetMapping("/user/list")
    public Result<List<User>> getUserList() {

        List<User> userList = travelService.getAllValidUser();
        return Result.success(userList);
    }

    @PostMapping("/recommend")
    public Result<TravelRecommendVO> recommend(@Valid @RequestBody TravelRequestDTO travelRequestDTO) {
        System.out.println(travelRequestDTO.getCity());
        System.out.println(travelRequestDTO.getDays());
        System.out.println(travelRequestDTO.getBudget());
        TravelRecommendVO travelRecommendVO = travelService.recommend(
                travelRequestDTO.getCity(),
                travelRequestDTO.getDays(),
                travelRequestDTO.getBudget()
        );
        System.out.println(travelRecommendVO);
        return Result.success(travelRecommendVO);
    }

    @PostMapping(value = "/chat" , produces = "text/event-stream")
    public SseEmitter chat(@Valid @RequestBody ChatRequestDTO chatRequestDTO){
        try {
            return travelService.chat(chatRequestDTO.getMessage());
        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }


}
