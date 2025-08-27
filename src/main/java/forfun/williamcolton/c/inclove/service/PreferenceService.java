package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.preference.req.CreatePreferenceDto;
import forfun.williamcolton.c.inclove.entity.UserPreference;

public interface PreferenceService extends IBaseService<UserPreference> {

    void createPreference(CreatePreferenceDto createPreferenceDto, String userId);

}
