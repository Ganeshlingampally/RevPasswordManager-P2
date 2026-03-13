package com.revpasswordmanager.mapper;

import com.revpasswordmanager.dto.SecurityQuestionInput;
import com.revpasswordmanager.model.SecurityQuestion;
import org.springframework.stereotype.Component;

@Component
public class SecurityQuestionMapper {

    public SecurityQuestion toEntity(SecurityQuestionInput input, String encodedAnswer) {
        if (input == null) {
            return null;
        }
        SecurityQuestion sq = new SecurityQuestion();
        sq.setQuestion(input.getQuestion());
        sq.setAnswerHash(encodedAnswer);
        return sq;
    }
}
