package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.CmsEntities.CmsFormSubmission;
import com.quantaida.mc.mapper.CmsFormSubmissionMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/form-submissions")
public class FormSubmissionController extends BaseCrudController<CmsFormSubmission> {
    public FormSubmissionController(CmsFormSubmissionMapper mapper) {
        super(mapper, "form", "name,phone");
    }
}
