package com.example.backend.api.controller.users;

import com.example.backend.api.dto.foods.response.ResponseTasteInfo;
import com.example.backend.api.dto.users.request.RequestTasteEvaluations;
import com.example.backend.api.dto.users.response.ResponseUserInfo;
import com.example.backend.api.entity.users.Users;
import com.example.backend.api.service.jwt.JwtService;
import com.example.backend.api.service.users.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "사용자 API")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;
    private final JwtService jwtService;

    @Operation(summary = "로그아웃", description = "사용자의 <strong>refresh token을 삭제</strong>합니다.")
    @PutMapping("/signout")
    public ResponseEntity<String> signOutUser(@AuthenticationPrincipal User users) {
        if (usersService.signOutUser(Long.parseLong(users.getUsername())).getToken() == null) {
            return new ResponseEntity<>("signOut 성공", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("signOut 실패", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "사용자 정보 조회", description = "사용자의 평균 <strong>맛 정보</strong>(맵기, 달기, 짜기, 느끼)와 <strong>알람 설정 정보</strong>를 조회합니다.")
    @GetMapping()
    public ResponseEntity<ResponseUserInfo> getUserIno(@AuthenticationPrincipal User users) {
        return new ResponseEntity<>(usersService.getUserInfo(Long.parseLong(users.getUsername())),
            HttpStatus.OK);
    }

    @Operation(summary = "초기 맛 평가 등록", description = "최소 5개 이상 <strong>초기 맛 평가 정보</strong>(맵기, 달기, 짜기, 느끼)를 등록합니다. <strong>사용자 ROLE</strong>을 업데이트합니다.")
    @PostMapping()
    public ResponseEntity<?> registInitialEvaluations(@AuthenticationPrincipal User users,
        @RequestBody List<RequestTasteEvaluations> requestTasteEvaluations) {

        Users user = usersService.registInitialEvaluations(Long.parseLong(users.getUsername()),
            requestTasteEvaluations);
        return new ResponseEntity<>(
            jwtService.createAccessToken(user.getEmail(), user.getNickname(),
                String.valueOf(user.getRole())), HttpStatus.OK);

    }

    @Operation(summary = "초기 맛 평가 등록 후 recommends 생성", description = "bigdata 서버에 recommends를 테이블에 데이터를 담으라고 요청한다.")
    @PostMapping("/recommends")
    public ResponseEntity<?> requestRegistRecommends(@AuthenticationPrincipal User users) {

        usersService.requestRegistRecommends(Long.parseLong(users.getUsername()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/taste")
    @Operation(summary = "맛 평가 등록", description = "<strong>맛 평가 정보(맵기, 달기, 짜기, 느끼)</strong>를 등록합니다.")
    public ResponseEntity<?> registEvaluations(@AuthenticationPrincipal User users,
        @RequestBody RequestTasteEvaluations requestTasteEvaluations) {

            usersService.registEvaluations(Long.parseLong(users.getUsername()),
                requestTasteEvaluations);
            return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/taste/{id}")
    @Operation(summary = "맛 평가 조회", description = "사용자의 <strong>맛 평가 정보(맵기, 달기, 짜기, 느끼)</strong>를 조회합니다.")
    public ResponseEntity<?> getEvaluations(@AuthenticationPrincipal User users,
        @PathVariable Long id) {
        try {
            ResponseTasteInfo responseTasteInfo = usersService.getEvaluations(
                Long.parseLong(users.getUsername()), id);
            return new ResponseEntity<>(responseTasteInfo, HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }
}
