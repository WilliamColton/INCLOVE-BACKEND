package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.interest.req.CreateInterestsDto;
import forfun.williamcolton.c.inclove.dto.interest.resp.GetAllInterestsResp;
import forfun.williamcolton.c.inclove.service.InterestService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interest")
public class InterestController {

    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @PostMapping
    public void createInterests(@RequestBody @Valid CreateInterestsDto createInterestsDto, Authentication authentication) {
        interestService.createInterests(createInterestsDto, (String) authentication.getPrincipal());
    }

    @GetMapping
    public GetAllInterestsResp getAllInterests(Authentication authentication) {
        return interestService.getAllInterests((String) authentication.getPrincipal());
    }

}
