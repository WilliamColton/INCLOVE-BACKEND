package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.preference.req.CreatePreferenceDto;
import forfun.williamcolton.c.inclove.service.PreferenceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preference")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    public void createPreference(@RequestBody CreatePreferenceDto createPreferenceDto, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        preferenceService.createPreference(createPreferenceDto, userId);
    }

}
