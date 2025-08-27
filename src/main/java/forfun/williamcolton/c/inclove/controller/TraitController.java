package forfun.williamcolton.c.inclove.controller;

import forfun.williamcolton.c.inclove.dto.trait.req.CreateTraitsDto;
import forfun.williamcolton.c.inclove.dto.trait.resp.GetAllTraitsResp;
import forfun.williamcolton.c.inclove.service.TraitService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trait")
public class TraitController {

    private final TraitService traitService;

    public TraitController(TraitService interestService) {
        this.traitService = interestService;
    }

    @PostMapping
    public void createInterests(@RequestBody @Valid CreateTraitsDto createTraitsDto, Authentication authentication) {
        traitService.createTraits(createTraitsDto, (String) authentication.getPrincipal());
    }

    @GetMapping
    public GetAllTraitsResp getAllInterests(Authentication authentication) {
        return traitService.getAllTraits((String) authentication.getPrincipal());
    }

}
