/** 后台资源元数据：驱动通用 CRUD 页面，字段与后端实体保持一致。 */
export interface Field {
  key: string
  label: string
  type?:
    | 'text'
    | 'textarea'
    | 'richtext'
    | 'number'
    | 'select'
    | 'date'
    | 'datetime'
    | 'upload'
    | 'attachments'
  options?: Array<{ label: string; value: string | number }>
  width?: number
  required?: boolean
  placeholder?: string
  /** upload/attachments 是否允许多选，attachments 默认多选 */
  multiple?: boolean
  /** upload/attachments 接受的文件类型，如 image/* */
  accept?: string
  /** 上传时使用的业务类型，默认取字段 key */
  bizType?: string
}

export interface RowAction {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  confirm?: string
  ask?: string
  askDefault?: string
  path: (row: any, input?: string) => string
  method?: 'post' | 'put'
  body?: (row: any, input?: string) => any
}

export interface Resource {
  api: string
  columns: Field[]
  form?: Field[]
  readonly?: boolean
  actions?: RowAction[]
}

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'DISABLED' }
]

const yesNo = [
  { label: '是', value: 1 },
  { label: '否', value: 0 }
]

const orderStatus = [
  { label: '待处理', value: 'PENDING' },
  { label: '进行中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

export const resources: Record<string, Resource> = {
  // ---------- 平台端 ----------
  plans: {
    api: '/platform/plans',
    columns: [
      { key: 'code', label: '套餐编码', width: 120 },
      { key: 'name', label: '套餐名称', width: 140 },
      { key: 'price', label: '价格(元)', type: 'number', width: 110 },
      { key: 'billingCycle', label: '计费周期', width: 100 },
      { key: 'endCount', label: '端数量', type: 'number', width: 90 },
      { key: 'seatLimit', label: '席位', type: 'number', width: 90 },
      { key: 'mpAccountLimit', label: '公众号', type: 'number', width: 90 },
      { key: 'storageQuotaGb', label: '存储(GB)', type: 'number', width: 100 },
      { key: 'smsQuota', label: '短信', type: 'number', width: 90 },
      { key: 'apiQuota', label: 'API', type: 'number', width: 100 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'code', label: '套餐编码', required: true },
      { key: 'name', label: '套餐名称', required: true },
      { key: 'price', label: '价格(元)', type: 'number', required: true },
      { key: 'billingCycle', label: '计费周期', type: 'select', options: [{ label: '月付', value: 'MONTH' }, { label: '年付', value: 'YEAR' }] },
      { key: 'endCount', label: '端数量', type: 'number' },
      { key: 'seatLimit', label: '席位上限', type: 'number' },
      { key: 'mpAccountLimit', label: '公众号上限', type: 'number' },
      { key: 'storageQuotaGb', label: '存储配额(GB)', type: 'number' },
      { key: 'smsQuota', label: '短信配额', type: 'number' },
      { key: 'apiQuota', label: 'API 配额', type: 'number' },
      { key: 'trialDays', label: '试用天数', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'features', label: '套餐特性', type: 'textarea' }
    ]
  },
  notices: {
    api: '/platform/notices',
    columns: [
      { key: 'title', label: '公告标题' },
      { key: 'targetType', label: '目标', width: 100 },
      { key: 'status', label: '状态', width: 100 },
      { key: 'publishedAt', label: '发布时间', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'title', label: '公告标题', required: true },
      { key: 'targetType', label: '目标类型', type: 'select', options: [{ label: '全部租户', value: 'ALL' }, { label: '指定套餐', value: 'PLAN' }, { label: '指定租户', value: 'TENANT' }] },
      { key: 'targetTenantIds', label: '目标 ID（逗号分隔）' },
      { key: 'status', label: '状态', type: 'select', options: [{ label: '草稿', value: 'DRAFT' }, { label: '已发布', value: 'PUBLISHED' }] },
      { key: 'content', label: '公告内容', type: 'textarea' }
    ],
    actions: [{ label: '发布', type: 'success', confirm: '确认发布该公告？', path: (r) => `/platform/notices/${r.id}/publish` }]
  },
  platformAudit: {
    api: '/platform/audit',
    readonly: true,
    columns: [
      { key: 'operatorName', label: '操作人', width: 120 },
      { key: 'tenantId', label: '租户 ID', width: 160 },
      { key: 'action', label: '动作' },
      { key: 'target', label: '对象' },
      { key: 'result', label: '结果', width: 90 },
      { key: 'ip', label: 'IP', width: 130 },
      { key: 'createdAt', label: '时间', type: 'datetime', width: 170 }
    ]
  },

  // ---------- 租户端 ----------
  members: {
    api: '/tenant/members',
    columns: [
      { key: 'memberNo', label: '会员编号', width: 150 },
      { key: 'name', label: '姓名', width: 110 },
      { key: 'phone', label: '手机号', width: 130 },
      { key: 'gender', label: '性别', width: 70 },
      { key: 'level', label: '等级', width: 90 },
      { key: 'points', label: '积分', type: 'number', width: 80 },
      { key: 'balance', label: '余额', type: 'number', width: 90 },
      { key: 'source', label: '来源', width: 90 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'name', label: '姓名', required: true },
      { key: 'phone', label: '手机号', placeholder: '保存时自动加密脱敏' },
      { key: 'gender', label: '性别', type: 'select', options: [{ label: '男', value: 'M' }, { label: '女', value: 'F' }, { label: '未知', value: 'U' }] },
      { key: 'birthday', label: '生日', type: 'date' },
      { key: 'level', label: '等级', type: 'select', options: [{ label: '普通', value: 'NORMAL' }, { label: '银卡', value: 'SILVER' }, { label: '金卡', value: 'GOLD' }] },
      { key: 'points', label: '积分', type: 'number' },
      { key: 'source', label: '来源', type: 'select', options: [{ label: '门店', value: 'STORE' }, { label: '小程序', value: 'MINI' }, { label: '渠道', value: 'DIST' }] },
      { key: 'tags', label: '标签（逗号分隔）' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  babies: {
    api: '/tenant/baby',
    columns: [
      { key: 'memberId', label: '会员 ID', width: 150 },
      { key: 'name', label: '宝宝姓名', width: 120 },
      { key: 'gender', label: '性别', width: 70 },
      { key: 'birthday', label: '生日', type: 'date', width: 120 },
      { key: 'allergy', label: '过敏源' },
      { key: 'healthNote', label: '健康备注' }
    ],
    form: [
      { key: 'memberId', label: '会员 ID', type: 'number', required: true },
      { key: 'name', label: '宝宝姓名', required: true },
      { key: 'gender', label: '性别', type: 'select', options: [{ label: '男', value: 'M' }, { label: '女', value: 'F' }] },
      { key: 'birthday', label: '生日', type: 'date' },
      { key: 'allergy', label: '过敏源' },
      { key: 'healthNote', label: '健康备注', type: 'textarea' }
    ]
  },
  appointments: {
    api: '/tenant/appointments',
    columns: [
      { key: 'appointmentNo', label: '预约单号', width: 160 },
      { key: 'memberId', label: '会员 ID', width: 150 },
      { key: 'storeId', label: '门店 ID', width: 140 },
      { key: 'serviceId', label: '服务 ID', width: 140 },
      { key: 'staffId', label: '服务人员', width: 140 },
      { key: 'appointDate', label: '预约日期', type: 'date', width: 110 },
      { key: 'timeSlot', label: '时段', width: 100 },
      { key: 'contact', label: '联系人', width: 100 },
      { key: 'phone', label: '电话', width: 120 },
      { key: 'status', label: '状态', width: 100 }
    ],
    form: [
      { key: 'memberId', label: '会员 ID', type: 'number' },
      { key: 'babyId', label: '宝宝 ID', type: 'number' },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'serviceId', label: '服务 ID', type: 'number' },
      { key: 'staffId', label: '服务人员 ID', type: 'number' },
      { key: 'appointDate', label: '预约日期', type: 'date' },
      { key: 'timeSlot', label: '时段', placeholder: '如 09:00-10:00' },
      { key: 'appointType', label: '预约方式', type: 'select', options: [{ label: '到店', value: 'STORE' }, { label: '上门', value: 'HOME' }] },
      { key: 'address', label: '上门地址' },
      { key: 'contact', label: '联系人' },
      { key: 'phone', label: '联系电话' },
      { key: 'status', label: '状态', type: 'select', options: orderStatus },
      { key: 'remark', label: '备注', type: 'textarea' }
    ],
    actions: [
      {
        label: '派单',
        type: 'primary',
        ask: '请输入派单员工 ID',
        path: (r, v) => `/tenant/appointments/${r.id}/dispatch?staffId=${v}`
      },
      { label: '取消', type: 'danger', confirm: '确认取消该预约？', path: (r) => `/tenant/appointments/${r.id}/cancel` }
    ]
  },
  orders: {
    api: '/tenant/orders',
    columns: [
      { key: 'orderNo', label: '订单号', width: 170 },
      { key: 'memberId', label: '会员 ID', width: 150 },
      { key: 'orderType', label: '类型', width: 100 },
      { key: 'totalAmount', label: '订单金额', type: 'number', width: 110 },
      { key: 'payAmount', label: '实付金额', type: 'number', width: 110 },
      { key: 'payStatus', label: '支付状态', width: 100 },
      { key: 'status', label: '订单状态', width: 100 },
      { key: 'source', label: '来源', width: 90 },
      { key: 'createdAt', label: '下单时间', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'memberId', label: '会员 ID', type: 'number' },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'orderType', label: '订单类型', type: 'select', options: [{ label: '商品', value: 'PRODUCT' }, { label: '服务', value: 'SERVICE' }] },
      { key: 'totalAmount', label: '订单金额', type: 'number' },
      { key: 'payAmount', label: '实付金额', type: 'number' },
      { key: 'payStatus', label: '支付状态', type: 'select', options: [{ label: '未支付', value: 'UNPAID' }, { label: '已支付', value: 'PAID' }, { label: '已退款', value: 'REFUNDED' }] },
      { key: 'status', label: '订单状态', type: 'select', options: orderStatus },
      { key: 'remark', label: '备注', type: 'textarea' }
    ],
    actions: [
      { label: '标记已支付', type: 'success', confirm: '确认将该订单标记为已支付？', path: (r) => `/tenant/orders/${r.id}/pay?channel=MOCK` },
      { label: '取消', type: 'warning', confirm: '确认取消该订单？', path: (r) => `/tenant/orders/${r.id}/cancel` },
      {
        label: '退款',
        type: 'danger',
        ask: '请输入退款金额',
        path: (r) => `/tenant/orders/${r.id}/refund`,
        body: (_r, v) => ({ amount: Number(v), reason: '后台退款' })
      }
    ]
  },
  products: {
    api: '/tenant/product',
    columns: [
      { key: 'spuCode', label: 'SPU 编码', width: 140 },
      { key: 'name', label: '商品名称' },
      { key: 'category', label: '分类', width: 110 },
      { key: 'brand', label: '品牌', width: 110 },
      { key: 'ageRange', label: '适用年龄', width: 110 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'spuCode', label: 'SPU 编码' },
      { key: 'name', label: '商品名称', required: true },
      { key: 'category', label: '分类' },
      { key: 'brand', label: '品牌' },
      { key: 'ageRange', label: '适用年龄' },
      { key: 'cover', label: '封面图', type: 'upload', accept: 'image/*', bizType: 'cover' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'detail', label: '商品详情', type: 'richtext' }
    ]
  },
  skus: {
    api: '/tenant/sku',
    columns: [
      { key: 'skuCode', label: 'SKU 编码', width: 140 },
      { key: 'productId', label: '商品 ID', width: 150 },
      { key: 'specJson', label: '规格' },
      { key: 'price', label: '售价', type: 'number', width: 100 },
      { key: 'costPrice', label: '成本价', type: 'number', width: 100 },
      { key: 'stock', label: '库存', type: 'number', width: 90 },
      { key: 'safetyStock', label: '安全库存', type: 'number', width: 100 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'productId', label: '商品 ID', type: 'number', required: true },
      { key: 'skuCode', label: 'SKU 编码', required: true },
      { key: 'specJson', label: '规格 JSON' },
      { key: 'price', label: '售价', type: 'number' },
      { key: 'costPrice', label: '成本价', type: 'number' },
      { key: 'stock', label: '库存', type: 'number' },
      { key: 'safetyStock', label: '安全库存', type: 'number' },
      { key: 'supplierId', label: '供应商 ID', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  stores: {
    api: '/tenant/store',
    columns: [
      { key: 'code', label: '门店编码', width: 120 },
      { key: 'name', label: '门店名称' },
      { key: 'address', label: '地址' },
      { key: 'phone', label: '电话', width: 130 },
      { key: 'businessHours', label: '营业时间', width: 130 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'code', label: '门店编码', required: true },
      { key: 'name', label: '门店名称', required: true },
      { key: 'address', label: '地址' },
      { key: 'phone', label: '电话' },
      { key: 'businessHours', label: '营业时间' },
      { key: 'serviceRange', label: '服务范围' },
      { key: 'lng', label: '经度', type: 'number' },
      { key: 'lat', label: '纬度', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  services: {
    api: '/tenant/service',
    columns: [
      { key: 'code', label: '服务编码', width: 120 },
      { key: 'name', label: '服务名称' },
      { key: 'category', label: '分类', width: 110 },
      { key: 'price', label: '价格', type: 'number', width: 100 },
      { key: 'duration', label: '时长(分钟)', type: 'number', width: 110 },
      { key: 'ageRange', label: '适用年龄', width: 110 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'code', label: '服务编码' },
      { key: 'name', label: '服务名称', required: true },
      { key: 'category', label: '分类' },
      { key: 'price', label: '价格', type: 'number' },
      { key: 'duration', label: '时长(分钟)', type: 'number' },
      { key: 'ageRange', label: '适用年龄' },
      { key: 'cover', label: '封面图', type: 'upload', accept: 'image/*', bizType: 'cover' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'description', label: '服务说明', type: 'richtext' }
    ]
  },
  schedules: {
    api: '/tenant/schedule',
    columns: [
      { key: 'staffId', label: '员工 ID', width: 150 },
      { key: 'storeId', label: '门店 ID', width: 140 },
      { key: 'workDate', label: '排班日期', type: 'date', width: 120 },
      { key: 'timeSlot', label: '时段', width: 120 },
      { key: 'capacity', label: '容量', type: 'number', width: 90 },
      { key: 'booked', label: '已约', type: 'number', width: 90 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'staffId', label: '员工 ID', type: 'number', required: true },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'workDate', label: '排班日期', type: 'date', required: true },
      { key: 'timeSlot', label: '时段' },
      { key: 'capacity', label: '容量', type: 'number' },
      { key: 'booked', label: '已约', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  coupons: {
    api: '/tenant/coupon',
    columns: [
      { key: 'name', label: '卡券名称' },
      { key: 'couponType', label: '类型', width: 100 },
      { key: 'faceValue', label: '面值', type: 'number', width: 100 },
      { key: 'minAmount', label: '门槛', type: 'number', width: 100 },
      { key: 'total', label: '总量', type: 'number', width: 90 },
      { key: 'issued', label: '已发', type: 'number', width: 90 },
      { key: 'validTo', label: '有效期至', type: 'date', width: 120 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'name', label: '卡券名称', required: true },
      { key: 'couponType', label: '类型', type: 'select', options: [{ label: '满减券', value: 'CASH' }, { label: '折扣券', value: 'DISCOUNT' }, { label: '体验券', value: 'TRIAL' }] },
      { key: 'faceValue', label: '面值', type: 'number' },
      { key: 'minAmount', label: '使用门槛', type: 'number' },
      { key: 'total', label: '发放总量', type: 'number' },
      { key: 'validFrom', label: '生效日期', type: 'date' },
      { key: 'validTo', label: '失效日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  activities: {
    api: '/tenant/activity',
    columns: [
      { key: 'title', label: '活动标题' },
      { key: 'startAt', label: '开始时间', type: 'datetime', width: 170 },
      { key: 'endAt', label: '结束时间', type: 'datetime', width: 170 },
      { key: 'address', label: '地点' },
      { key: 'capacity', label: '名额', type: 'number', width: 90 },
      { key: 'joined', label: '已报名', type: 'number', width: 90 },
      { key: 'price', label: '价格', type: 'number', width: 100 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'title', label: '活动标题', required: true },
      { key: 'startAt', label: '开始时间', type: 'datetime' },
      { key: 'endAt', label: '结束时间', type: 'datetime' },
      { key: 'address', label: '活动地点' },
      { key: 'capacity', label: '活动名额', type: 'number' },
      { key: 'joined', label: '已报名人数', type: 'number' },
      { key: 'price', label: '活动价格', type: 'number' },
      { key: 'cover', label: '封面图', type: 'upload', accept: 'image/*', bizType: 'cover' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'content', label: '活动详情', type: 'richtext' }
    ]
  },
  pointsRules: {
    api: '/tenant/points-rules',
    columns: [
      { key: 'name', label: '规则名称', width: 160 },
      { key: 'ruleType', label: '类型', width: 110 },
      { key: 'ratio', label: '比例/分值', type: 'number', width: 110 },
      { key: 'enabled', label: '启用', type: 'number', width: 80 },
      { key: 'createdAt', label: '创建时间', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'name', label: '规则名称', required: true },
      { key: 'ruleType', label: '类型', type: 'select', options: [{ label: '消费积分', value: 'CONSUME' }, { label: '签到积分', value: 'SIGN' }, { label: '活动积分', value: 'ACTIVITY' }] },
      { key: 'ratio', label: '比例/分值', type: 'number' },
      { key: 'enabled', label: '启用', type: 'number' }
    ]
  },
  pointsFlows: {
    api: '/tenant/points-flows',
    columns: [
      { key: 'memberId', label: '会员 ID', width: 160 },
      { key: 'flowType', label: '类型', width: 110 },
      { key: 'points', label: '变动', type: 'number', width: 90 },
      { key: 'balance', label: '余额', type: 'number', width: 90 },
      { key: 'bizNo', label: '业务单号', width: 150 },
      { key: 'remark', label: '备注' },
      { key: 'createdAt', label: '时间', type: 'datetime', width: 170 }
    ],
    readonly: true
  },
  suppliers: {    api: '/tenant/supplier',
    columns: [
      { key: 'code', label: '供应商编码', width: 130 },
      { key: 'name', label: '供应商名称' },
      { key: 'contact', label: '联系人', width: 110 },
      { key: 'phone', label: '电话', width: 130 },
      { key: 'rating', label: '评级', width: 90 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'code', label: '供应商编码', required: true },
      { key: 'name', label: '供应商名称', required: true },
      { key: 'contact', label: '联系人' },
      { key: 'phone', label: '电话' },
      { key: 'address', label: '地址' },
      { key: 'agreementNo', label: '协议编号' },
      { key: 'rating', label: '评级' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  supplyOrders: {
    api: '/tenant/supply-orders',
    columns: [
      { key: 'supplyNo', label: '供货单号', width: 160 },
      { key: 'supplierId', label: '供应商 ID', width: 150 },
      { key: 'storeId', label: '门店 ID', width: 140 },
      { key: 'totalAmount', label: '金额', type: 'number', width: 110 },
      { key: 'status', label: '状态', width: 100 },
      { key: 'expectedAt', label: '预计到货', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'supplyNo', label: '供货单号' },
      { key: 'supplierId', label: '供应商 ID', type: 'number' },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'totalAmount', label: '金额', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: [{ label: '待确认', value: 'PENDING' }, { label: '已发货', value: 'SHIPPED' }, { label: '已签收', value: 'SIGNED' }] },
      { key: 'expectedAt', label: '预计到货', type: 'datetime' }
    ]
  },
  distributors: {
    api: '/tenant/distributor',
    columns: [
      { key: 'code', label: '渠道商编码', width: 130 },
      { key: 'name', label: '渠道商名称' },
      { key: 'contact', label: '联系人', width: 110 },
      { key: 'phone', label: '电话', width: 130 },
      { key: 'parentId', label: '上级 ID', width: 150 },
      { key: 'creditLimit', label: '授信额度', type: 'number', width: 120 },
      { key: 'usedCredit', label: '已用额度', type: 'number', width: 120 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'code', label: '渠道商编码', required: true },
      { key: 'name', label: '渠道商名称', required: true },
      { key: 'contact', label: '联系人' },
      { key: 'phone', label: '电话' },
      { key: 'parentId', label: '上级渠道商 ID', type: 'number' },
      { key: 'commissionRule', label: '分佣规则 JSON' },
      { key: 'creditLimit', label: '授信额度', type: 'number' },
      { key: 'usedCredit', label: '已用额度', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  distributorReports: {
    api: '/tenant/distributor-reports',
    columns: [
      { key: 'reportNo', label: '报单号', width: 160 },
      { key: 'distributorId', label: '渠道商 ID', width: 150 },
      { key: 'memberId', label: '会员 ID', width: 150 },
      { key: 'orderId', label: '订单 ID', width: 150 },
      { key: 'amount', label: '金额', type: 'number', width: 110 },
      { key: 'commission', label: '佣金', type: 'number', width: 110 },
      { key: 'status', label: '状态', width: 100 }
    ],
    form: [
      { key: 'reportNo', label: '报单号' },
      { key: 'distributorId', label: '渠道商 ID', type: 'number' },
      { key: 'memberId', label: '会员 ID', type: 'number' },
      { key: 'orderId', label: '订单 ID', type: 'number' },
      { key: 'amount', label: '金额', type: 'number' },
      { key: 'commission', label: '佣金', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: [{ label: '待审核', value: 'PENDING' }, { label: '已确认', value: 'CONFIRMED' }, { label: '已结算', value: 'SETTLED' }] }
    ]
  },
  staff: {
    api: '/tenant/staff',
    columns: [
      { key: 'name', label: '姓名', width: 110 },
      { key: 'roleType', label: '岗位', width: 120 },
      { key: 'phone', label: '手机号', width: 130 },
      { key: 'level', label: '等级', width: 90 },
      { key: 'storeId', label: '门店 ID', width: 140 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'name', label: '姓名', required: true },
      { key: 'roleType', label: '岗位', type: 'select', options: [{ label: '母婴护理师', value: 'NURSE' }, { label: '育婴师', value: 'NANNY' }, { label: '店长', value: 'MANAGER' }] },
      { key: 'phone', label: '手机号' },
      { key: 'level', label: '等级' },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'userId', label: '关联账号 ID', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  licenses: {
    api: '/tenant/staff-licenses',
    columns: [
      { key: 'staffId', label: '员工 ID', width: 150 },
      { key: 'licenseType', label: '证照类型', width: 130 },
      { key: 'licenseNo', label: '证照编号', width: 160 },
      { key: 'expireAt', label: '到期日', type: 'date', width: 120 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'staffId', label: '员工 ID', type: 'number', required: true },
      { key: 'licenseType', label: '证照类型' },
      { key: 'licenseNo', label: '证照编号' },
      { key: 'expireAt', label: '到期日', type: 'date' },
      { key: 'fileUrl', label: '证照文件' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  articles: {
    api: '/tenant/article',
    columns: [
      { key: 'title', label: '标题' },
      { key: 'category', label: '分类', width: 110 },
      { key: 'status', label: '状态', width: 100 },
      { key: 'publishedAt', label: '发布时间', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'title', label: '标题', required: true },
      { key: 'titleEn', label: '英文标题' },
      { key: 'category', label: '分类' },
      { key: 'cover', label: '封面图', type: 'upload', accept: 'image/*', bizType: 'cover' },
      { key: 'status', label: '状态', type: 'select', options: [{ label: '草稿', value: 'DRAFT' }, { label: '已发布', value: 'PUBLISHED' }] },
      { key: 'publishedAt', label: '发布时间', type: 'datetime' },
      { key: 'seoTitle', label: 'SEO 标题' },
      { key: 'seoKeywords', label: 'SEO 关键词' },
      { key: 'seoDesc', label: 'SEO 描述', type: 'textarea' },
      { key: 'content', label: '正文', type: 'richtext' }
    ]
  },
  banners: {
    api: '/tenant/banner',
    columns: [
      { key: 'terminal', label: '终端', width: 100 },
      { key: 'title', label: '标题' },
      { key: 'image', label: '图片地址' },
      { key: 'link', label: '跳转链接' },
      { key: 'sortNo', label: '排序', type: 'number', width: 80 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'terminal', label: '终端', type: 'select', options: [{ label: '官网', value: 'PC' }, { label: '小程序', value: 'MINI' }, { label: 'H5', value: 'H5' }, { label: 'App', value: 'APP' }] },
      { key: 'title', label: '标题' },
      { key: 'image', label: '图片地址' },
      { key: 'link', label: '跳转链接' },
      { key: 'sortNo', label: '排序', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  cases: {
    api: '/tenant/cases',
    columns: [
      { key: 'title', label: '案例标题' },
      { key: 'sortNo', label: '排序', type: 'number', width: 80 },
      { key: 'status', label: '状态', width: 90 }
    ],
    form: [
      { key: 'title', label: '案例标题', required: true },
      { key: 'titleEn', label: '英文标题' },
      { key: 'cover', label: '封面图', type: 'upload', accept: 'image/*', bizType: 'cover' },
      { key: 'sortNo', label: '排序', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions },
      { key: 'content', label: '案例内容', type: 'richtext' }
    ]
  },
  users: {
    api: '/tenant/users',
    columns: [
      { key: 'username', label: '账号', width: 130 },
      { key: 'nickname', label: '昵称', width: 130 },
      { key: 'phone', label: '手机号', width: 130 },
      { key: 'userType', label: '类型', width: 110 },
      { key: 'roleId', label: '角色 ID', width: 150 },
      { key: 'storeId', label: '门店 ID', width: 140 },
      { key: 'status', label: '状态', width: 90 },
      { key: 'lastLoginAt', label: '最近登录', type: 'datetime', width: 170 }
    ],
    form: [
      { key: 'username', label: '账号', required: true },
      { key: 'password', label: '密码', placeholder: '留空默认 admin123' },
      { key: 'nickname', label: '昵称' },
      { key: 'phone', label: '手机号' },
      { key: 'email', label: '邮箱' },
      { key: 'roleId', label: '角色 ID', type: 'number' },
      { key: 'storeId', label: '门店 ID', type: 'number' },
      { key: 'staffId', label: '员工 ID', type: 'number' },
      { key: 'supplierId', label: '供应商 ID', type: 'number' },
      { key: 'distributorId', label: '渠道商 ID', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: statusOptions }
    ]
  },
  roles: {
    api: '/tenant/roles',
    columns: [
      { key: 'name', label: '角色名称', width: 150 },
      { key: 'code', label: '角色编码', width: 170 },
      { key: 'dataScope', label: '数据范围', width: 130 },
      { key: 'remark', label: '备注' }
    ],
    form: [
      { key: 'name', label: '角色名称', required: true },
      { key: 'code', label: '角色编码', required: true },
      { key: 'dataScope', label: '数据范围', type: 'select', options: [{ label: '全部门店', value: 'ALL' }, { label: '本门店', value: 'STORE' }, { label: '仅本人', value: 'SELF' }] },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  auditLogs: {
    api: '/tenant/audit-logs',
    readonly: true,
    columns: [
      { key: 'userName', label: '操作人', width: 120 },
      { key: 'module', label: '模块', width: 120 },
      { key: 'action', label: '动作' },
      { key: 'method', label: '方法', width: 180 },
      { key: 'result', label: '结果', width: 90 },
      { key: 'ip', label: 'IP', width: 130 },
      { key: 'costMs', label: '耗时(ms)', type: 'number', width: 110 },
      { key: 'createdAt', label: '时间', type: 'datetime', width: 170 }
    ]
  }
}

export const permissionOptions = { statusOptions, yesNo }
