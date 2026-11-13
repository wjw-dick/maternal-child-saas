package com.quantaida.mc.web.tenant;

import com.quantaida.mc.entity.BizEntities.BizStockRecord;
import com.quantaida.mc.mapper.BizStockRecordMapper;
import com.quantaida.mc.web.BaseCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/stock-records")
public class StockRecordController extends BaseCrudController<BizStockRecord> {
    public StockRecordController(BizStockRecordMapper mapper) {
        super(mapper, "stock", "bizNo");
    }
}
