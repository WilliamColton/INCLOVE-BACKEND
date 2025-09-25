package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.annotation.Auth;
import forfun.williamcolton.c.inclove.dto.profile.req.CreateProfileDto;
import forfun.williamcolton.c.inclove.dto.profile.resp.GetUserIdDto;
import forfun.williamcolton.c.inclove.entity.UserProfile;
import forfun.williamcolton.c.inclove.service.MatchService;
import forfun.williamcolton.c.inclove.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final MatchService matchService;

    public ProfileController(ProfileService profileService, MatchService matchService) {
        this.profileService = profileService;
        this.matchService = matchService;
    }

    @PostMapping
    public void createProfile(@RequestBody @Valid CreateProfileDto createProfileDto, Authentication authentication) {
        profileService.createProfile(createProfileDto, (String) authentication.getPrincipal());
    }

    @GetMapping
    public List<UserProfile> match(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return matchService.match(userId);
    }

    @GetMapping("/me")
    public GetUserIdDto getCurrentUserId(@Auth Authentication authentication) {
        return new GetUserIdDto((String) authentication.getPrincipal());
    }

}
