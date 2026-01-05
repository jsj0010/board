package com.chip.board.register.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.domain.Department;
import com.chip.board.register.domain.Role;
import com.chip.board.register.domain.User;
import com.chip.board.register.dto.request.UserRegisterRequest;
import com.chip.board.register.dto.request.MailVerifyRequest;
import com.chip.board.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterService {

    private final StringRedisTemplate stringRedisTemplate;
    @Value("${verification.code.expiry-minutes}")
    private int verificationCodeExpiryMinutes;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<String> showRegisterForm() {
        return Department.showDepartment();
    }

    public void register(UserRegisterRequest userRegisterRequest) {
        String key = "auth:email:" + userRegisterRequest.getUsername();
        String verificationStatus = stringRedisTemplate.opsForValue().get(key);
        if (!"VERIFIED".equals(verificationStatus)) { // 이메일 인증이 아직 완료되지 않았을 때
            throw new ServiceException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        try {
            String encodedPassword=passwordEncoder.encode(userRegisterRequest.getPassword());
            Department departmentEnum = Department.fromDisplayName(userRegisterRequest.getDepartment());
            User user = User.builder()
                    .username(userRegisterRequest.getUsername())
                    .password(encodedPassword)
                    .name(userRegisterRequest.getName())
                    .department(departmentEnum.getDisplayName())
                    .studentId(userRegisterRequest.getStudentId())
                    .grade(userRegisterRequest.getGrade())
                    .role(Role.USER)
                    .bojId((userRegisterRequest.getBojId()))
                    .phoneNumber(userRegisterRequest.getPhoneNumber())
                    .build();
            userRepository.save(user);
            stringRedisTemplate.delete(key);
        }
        catch (Exception e) {
            throw new ServiceException(ErrorCode.UNEXPECTED_SERVER_ERROR);
        }
    }
    public void sendMail(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ServiceException(ErrorCode.USER_ALREADY_EXIST);
        }

        int number = createNumber();
        try {
            emailService.sendAuthCodeMailAsync(username, number).join();
        } catch (java.util.concurrent.CompletionException e) {
            throw new ServiceException(ErrorCode.EMAIL_SEND_ERROR);
        }
        String key="auth:email:"+username;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(number), Duration.ofMinutes(verificationCodeExpiryMinutes));
    }

    private int createNumber() { // 메일 코드 생성
        return new java.security.SecureRandom().nextInt(900000) + 100000; // 최소 100000인 6자리 숫자
    }

    public void checkVerificationNumber(MailVerifyRequest mailVerifyRequest) { // 이메일 코드 일치 검증
        String key="auth:email:"+mailVerifyRequest.getUsername();
        String requestCode=String.valueOf(mailVerifyRequest.getMailCode());
        String storedValue = stringRedisTemplate.opsForValue().get(key);// 인증 코드를 레디스에서 가져와야함
        if(storedValue==null){  // 저장된 인증번호가 없다
            throw new ServiceException(ErrorCode.EXPIRED_EMAIL_CODE);
        }
        if("VERIFIED".equals(storedValue))
            return;
        // 입력한 코드와 redis에 저장된 코드가 일치하지 않을 때
        if(!requestCode.equals(storedValue)){
            throw new ServiceException(ErrorCode.INVALID_EMAIL_CODE);
        }
        stringRedisTemplate.opsForValue().set(key, "VERIFIED", Duration.ofMinutes(verificationCodeExpiryMinutes));
    }
}

