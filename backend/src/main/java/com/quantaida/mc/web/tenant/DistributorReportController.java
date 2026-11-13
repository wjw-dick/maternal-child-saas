package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizDistributorReport;
import com.quantaida.mc.mapper.BizDistributorReportMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/distributor-reports")
public class DistributorReportController extends BaseCrudController<BizDistributorReport> {
    public DistributorReportController(BizDistributorReportMapper mapper) {
        super(mapper, "report", "reportNo");
    }
}
