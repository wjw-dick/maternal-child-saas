-- 母婴亲子 SaaS 数据库结构（MySQL 8 / H2 MySQL 模式通用）
-- 租户业务表统一 tenant_id；平台表 platform_* 不参与租户插件过滤

CREATE TABLE IF NOT EXISTS platform_plan (
  id BIGINT PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  price DECIMAL(12,2) DEFAULT 0,
  billing_cycle VARCHAR(32) DEFAULT 'MONTH',
  end_count INT DEFAULT 0,
  seat_limit INT DEFAULT 5,
  mp_account_limit INT DEFAULT 1,
  storage_quota_gb INT DEFAULT 10,
  sms_quota INT DEFAULT 1000,
  api_quota INT DEFAULT 10000,
  trial_days INT DEFAULT 14,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  features TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_plan_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS platform_tenant (
  id BIGINT PRIMARY KEY,
  tenant_code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  industry VARCHAR(64),
  contact VARCHAR(64),
  phone VARCHAR(64),
  plan_id BIGINT,
  status VARCHAR(32) DEFAULT 'TRIAL',
  trial_end_at DATETIME,
  expire_at DATETIME,
  seat_limit INT DEFAULT 5,
  mp_account_limit INT DEFAULT 1,
  storage_quota_gb INT DEFAULT 10,
  sms_quota INT DEFAULT 1000,
  api_quota INT DEFAULT 10000,
  remark VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_tenant_code UNIQUE (tenant_code)
);

CREATE TABLE IF NOT EXISTS platform_tenant_quota_usage (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  used_seats INT DEFAULT 0,
  used_mp INT DEFAULT 0,
  used_storage_mb BIGINT DEFAULT 0,
  used_sms INT DEFAULT 0,
  used_api INT DEFAULT 0,
  stat_date DATE,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_quota_usage UNIQUE (tenant_id, stat_date)
);

CREATE TABLE IF NOT EXISTS platform_bill (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  period VARCHAR(32),
  item VARCHAR(128),
  quantity INT DEFAULT 0,
  amount DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'UNPAID',
  paid_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS platform_audit (
  id BIGINT PRIMARY KEY,
  operator_type VARCHAR(32),
  operator_id BIGINT,
  operator_name VARCHAR(64),
  tenant_id BIGINT,
  action VARCHAR(128),
  target VARCHAR(128),
  result VARCHAR(32),
  ip VARCHAR(64),
  detail TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS platform_tenant_admin (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_primary INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS platform_notice (
  id BIGINT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  content TEXT,
  target_type VARCHAR(32) DEFAULT 'ALL',
  target_tenant_ids VARCHAR(1024),
  status VARCHAR(32) DEFAULT 'DRAFT',
  published_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS platform_notice_read (
  id BIGINT PRIMARY KEY,
  notice_id BIGINT NOT NULL,
  tenant_id BIGINT,
  user_id BIGINT,
  read_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(128),
  nickname VARCHAR(64),
  phone VARCHAR(64),
  email VARCHAR(128),
  avatar VARCHAR(512),
  user_type VARCHAR(32) DEFAULT 'TENANT',
  role_id BIGINT,
  store_id BIGINT,
  staff_id BIGINT,
  supplier_id BIGINT,
  distributor_id BIGINT,
  openid VARCHAR(128),
  unionid VARCHAR(128),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  last_login_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_user_name UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  data_scope VARCHAR(32) DEFAULT 'ALL',
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_role_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  parent_id BIGINT DEFAULT 0,
  scope VARCHAR(32) DEFAULT 'TENANT',
  name VARCHAR(64) NOT NULL,
  code VARCHAR(96),
  menu_type VARCHAR(16) DEFAULT 'MENU',
  path VARCHAR(256),
  component VARCHAR(256),
  icon VARCHAR(64),
  sort_no INT DEFAULT 0,
  visible INT DEFAULT 1,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS sys_audit_log (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  user_id BIGINT,
  user_name VARCHAR(64),
  module VARCHAR(64),
  action VARCHAR(96),
  method VARCHAR(256),
  params TEXT,
  ip VARCHAR(64),
  result VARCHAR(32),
  cost_ms BIGINT DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  config_key VARCHAR(96) NOT NULL,
  config_value TEXT,
  group_code VARCHAR(32),
  encrypted INT DEFAULT 0,
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_config_key UNIQUE (tenant_id, config_key)
);

CREATE TABLE IF NOT EXISTS sys_language (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  code VARCHAR(16) NOT NULL,
  name VARCHAR(32),
  enabled INT DEFAULT 1,
  is_default INT DEFAULT 0,
  sort_no INT DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_language_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS sys_i18n (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  lang_code VARCHAR(16) NOT NULL,
  i18n_key VARCHAR(128) NOT NULL,
  i18n_value TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_i18n_key UNIQUE (tenant_id, lang_code, i18n_key)
);

CREATE TABLE IF NOT EXISTS sys_menu_config (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  terminal VARCHAR(32) NOT NULL,
  tree_json TEXT,
  version INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_menu_terminal UNIQUE (tenant_id, terminal)
);

CREATE TABLE IF NOT EXISTS sys_storage_config (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  driver VARCHAR(32) DEFAULT 'LOCAL',
  config_json TEXT,
  enabled INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_storage_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS sys_message_channel (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  channel VARCHAR(32) NOT NULL,
  enabled INT DEFAULT 0,
  config_json TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_msg_channel UNIQUE (tenant_id, channel)
);

CREATE TABLE IF NOT EXISTS sys_sms_channel (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  provider VARCHAR(32) NOT NULL,
  enabled INT DEFAULT 0,
  config_json TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_sms_provider UNIQUE (tenant_id, provider)
);

CREATE TABLE IF NOT EXISTS sys_message_template (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  node VARCHAR(64) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  template_id VARCHAR(128),
  title VARCHAR(128),
  field_mapping VARCHAR(512),
  enabled INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_msg_template UNIQUE (tenant_id, node, channel)
);

CREATE TABLE IF NOT EXISTS sys_sso_config (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  enabled INT DEFAULT 0,
  protocol VARCHAR(32) DEFAULT 'OIDC',
  base_url VARCHAR(256),
  client_id VARCHAR(128),
  client_secret VARCHAR(256),
  account_mapping VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_sso_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS sys_attachment (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  biz_type VARCHAR(64),
  biz_id BIGINT,
  name VARCHAR(256),
  url VARCHAR(512),
  size BIGINT DEFAULT 0,
  ext VARCHAR(32),
  driver VARCHAR(32) DEFAULT 'LOCAL',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  dict_code VARCHAR(64) NOT NULL,
  dict_name VARCHAR(128),
  scope VARCHAR(32) DEFAULT 'GLOBAL',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_dict_type UNIQUE (tenant_id, dict_code)
);

CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  dict_code VARCHAR(64) NOT NULL,
  item_value VARCHAR(64) NOT NULL,
  item_label VARCHAR(128),
  sort_no INT DEFAULT 0,
  status INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS open_app (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(128) NOT NULL,
  app_key VARCHAR(64) NOT NULL,
  app_secret VARCHAR(128),
  scopes VARCHAR(256),
  api_quota INT DEFAULT 10000,
  used_api INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_open_app_key UNIQUE (tenant_id, app_key)
);

-- ============ 业务表 ============
CREATE TABLE IF NOT EXISTS biz_member (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  member_no VARCHAR(64) NOT NULL,
  name VARCHAR(64),
  phone VARCHAR(64),
  phone_enc VARCHAR(256),
  phone_hash VARCHAR(128),
  avatar VARCHAR(512),
  id_card VARCHAR(64),
  id_card_enc VARCHAR(256),
  gender VARCHAR(16),
  birthday DATE,
  level VARCHAR(32) DEFAULT 'NORMAL',
  balance DECIMAL(12,2) DEFAULT 0,
  points INT DEFAULT 0,
  tags VARCHAR(512),
  source VARCHAR(32),
  openid VARCHAR(128),
  unionid VARCHAR(128),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  remark VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_member_no UNIQUE (tenant_id, member_no)
);

CREATE TABLE IF NOT EXISTS biz_baby (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  name VARCHAR(64),
  gender VARCHAR(16),
  birthday DATE,
  allergy VARCHAR(512),
  health_note VARCHAR(1024),
  avatar VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_baby_growth (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  baby_id BIGINT NOT NULL,
  record_date DATE,
  height DECIMAL(6,2),
  weight DECIMAL(6,2),
  note VARCHAR(512),
  images TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_store (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  address VARCHAR(256),
  phone VARCHAR(64),
  business_hours VARCHAR(128),
  service_range VARCHAR(512),
  delivery_range VARCHAR(512),
  lng DECIMAL(10,6),
  lat DECIMAL(10,6),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_store_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS biz_service (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  category VARCHAR(64),
  price DECIMAL(12,2) DEFAULT 0,
  duration INT DEFAULT 60,
  age_range VARCHAR(64),
  cover VARCHAR(512),
  description TEXT,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_service_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS biz_staff (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT,
  store_id BIGINT,
  name VARCHAR(64) NOT NULL,
  role_type VARCHAR(32),
  phone VARCHAR(64),
  level VARCHAR(32),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_staff_license (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  license_type VARCHAR(64),
  license_no VARCHAR(64),
  expire_at DATE,
  file_url VARCHAR(512),
  status VARCHAR(32) DEFAULT 'VALID',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_schedule (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  store_id BIGINT,
  work_date DATE,
  time_slot VARCHAR(32),
  capacity INT DEFAULT 1,
  booked INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'OPEN',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_product (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  spu_code VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  category VARCHAR(64),
  brand VARCHAR(64),
  age_range VARCHAR(64),
  cover VARCHAR(512),
  images TEXT,
  detail TEXT,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_product_code UNIQUE (tenant_id, spu_code)
);

CREATE TABLE IF NOT EXISTS biz_sku (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sku_code VARCHAR(64),
  spec_json VARCHAR(512),
  price DECIMAL(12,2) DEFAULT 0,
  cost_price DECIMAL(12,2) DEFAULT 0,
  stock INT DEFAULT 0,
  safety_stock INT DEFAULT 0,
  supplier_id BIGINT,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_sku_code UNIQUE (tenant_id, sku_code)
);

CREATE TABLE IF NOT EXISTS biz_stock_record (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  record_type VARCHAR(16),
  qty INT DEFAULT 0,
  before_qty INT DEFAULT 0,
  after_qty INT DEFAULT 0,
  biz_no VARCHAR(64),
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_order (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  member_id BIGINT,
  store_id BIGINT,
  order_type VARCHAR(32) DEFAULT 'PRODUCT',
  total_amount DECIMAL(12,2) DEFAULT 0,
  discount_amount DECIMAL(12,2) DEFAULT 0,
  pay_amount DECIMAL(12,2) DEFAULT 0,
  pay_status VARCHAR(32) DEFAULT 'UNPAID',
  status VARCHAR(32) DEFAULT 'PENDING',
  pay_channel VARCHAR(32),
  pay_time DATETIME,
  remark VARCHAR(512),
  source VARCHAR(32),
  idempotency_key VARCHAR(128),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_order_no UNIQUE (tenant_id, order_no),
  CONSTRAINT uk_order_idem UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE IF NOT EXISTS biz_order_item (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  item_type VARCHAR(32),
  item_id BIGINT,
  item_name VARCHAR(128),
  spec VARCHAR(256),
  price DECIMAL(12,2) DEFAULT 0,
  qty INT DEFAULT 1,
  amount DECIMAL(12,2) DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_payment (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  pay_no VARCHAR(64) NOT NULL,
  order_id BIGINT,
  channel VARCHAR(32),
  amount DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'PENDING',
  trade_no VARCHAR(128),
  paid_at DATETIME,
  raw_json TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_pay_no UNIQUE (tenant_id, pay_no)
);

CREATE TABLE IF NOT EXISTS biz_refund (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  refund_no VARCHAR(64) NOT NULL,
  order_id BIGINT,
  payment_id BIGINT,
  amount DECIMAL(12,2) DEFAULT 0,
  reason VARCHAR(256),
  status VARCHAR(32) DEFAULT 'PENDING',
  refunded_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_refund_no UNIQUE (tenant_id, refund_no)
);

CREATE TABLE IF NOT EXISTS biz_fulfillment (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  order_id BIGINT,
  fulfill_type VARCHAR(32),
  staff_id BIGINT,
  status VARCHAR(32),
  started_at DATETIME,
  finished_at DATETIME,
  sign_url VARCHAR(512),
  remark VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_appointment (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  appointment_no VARCHAR(64) NOT NULL,
  member_id BIGINT,
  baby_id BIGINT,
  store_id BIGINT,
  service_id BIGINT,
  staff_id BIGINT,
  appoint_date DATE,
  time_slot VARCHAR(32),
  appoint_type VARCHAR(16) DEFAULT 'STORE',
  address VARCHAR(256),
  contact VARCHAR(64),
  phone VARCHAR(64),
  status VARCHAR(32) DEFAULT 'PENDING',
  verify_code VARCHAR(16),
  remark VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_appointment_no UNIQUE (tenant_id, appointment_no)
);

CREATE TABLE IF NOT EXISTS biz_dispatch (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  appointment_id BIGINT NOT NULL,
  staff_id BIGINT,
  status VARCHAR(32) DEFAULT 'PENDING',
  accepted_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_checkin (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  appointment_id BIGINT NOT NULL,
  staff_id BIGINT,
  checkin_type VARCHAR(16),
  lng DECIMAL(10,6),
  lat DECIMAL(10,6),
  address VARCHAR(256),
  photo_url VARCHAR(512),
  checkin_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_service_record (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  appointment_id BIGINT NOT NULL,
  staff_id BIGINT,
  content TEXT,
  baby_temp DECIMAL(5,2),
  baby_weight DECIMAL(5,2),
  images TEXT,
  signed_url VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_wallet (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  balance DECIMAL(12,2) DEFAULT 0,
  total_recharge DECIMAL(12,2) DEFAULT 0,
  total_consume DECIMAL(12,2) DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_wallet_member UNIQUE (tenant_id, member_id)
);

CREATE TABLE IF NOT EXISTS biz_wallet_flow (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  flow_no VARCHAR(64),
  flow_type VARCHAR(16),
  amount DECIMAL(12,2) DEFAULT 0,
  balance DECIMAL(12,2) DEFAULT 0,
  biz_no VARCHAR(64),
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_points_rule (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(128),
  rule_type VARCHAR(32),
  ratio DECIMAL(10,2) DEFAULT 1,
  enabled INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_points_flow (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  flow_type VARCHAR(16),
  points INT DEFAULT 0,
  balance INT DEFAULT 0,
  biz_no VARCHAR(64),
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_coupon (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  coupon_type VARCHAR(16) DEFAULT 'CASH',
  face_value DECIMAL(12,2) DEFAULT 0,
  min_amount DECIMAL(12,2) DEFAULT 0,
  total INT DEFAULT 0,
  issued INT DEFAULT 0,
  valid_from DATE,
  valid_to DATE,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_member_coupon (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  coupon_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  status VARCHAR(32) DEFAULT 'UNUSED',
  used_at DATETIME,
  order_id BIGINT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_activity (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  title VARCHAR(256) NOT NULL,
  cover VARCHAR(512),
  start_at DATETIME,
  end_at DATETIME,
  address VARCHAR(256),
  capacity INT DEFAULT 0,
  joined INT DEFAULT 0,
  price DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  content TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_activity_signup (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  member_id BIGINT,
  baby_id BIGINT,
  phone VARCHAR(64),
  status VARCHAR(32) DEFAULT 'SIGNED',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_supplier (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  contact VARCHAR(64),
  phone VARCHAR(64),
  address VARCHAR(256),
  agreement_no VARCHAR(64),
  rating VARCHAR(16),
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_supplier_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS biz_supplier_qualification (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  license_type VARCHAR(64),
  license_no VARCHAR(128),
  file_url VARCHAR(512),
  expire_date DATE,
  audit_status VARCHAR(32) DEFAULT 'PENDING',
  remark VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_supply_order (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  supply_no VARCHAR(64) NOT NULL,
  supplier_id BIGINT,
  store_id BIGINT,
  total_amount DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'PENDING',
  expected_at DATETIME,
  signed_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_supply_no UNIQUE (tenant_id, supply_no)
);

CREATE TABLE IF NOT EXISTS biz_supply_item (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  supply_id BIGINT NOT NULL,
  sku_id BIGINT,
  name VARCHAR(128),
  qty INT DEFAULT 0,
  price DECIMAL(12,2) DEFAULT 0,
  amount DECIMAL(12,2) DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_supplier_statement (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  period VARCHAR(32),
  amount DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'UNSETTLED',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_distributor (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  contact VARCHAR(64),
  phone VARCHAR(64),
  parent_id BIGINT DEFAULT 0,
  commission_rule VARCHAR(64),
  credit_limit DECIMAL(12,2) DEFAULT 0,
  used_credit DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_distributor_code UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS biz_distributor_report (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  report_no VARCHAR(64) NOT NULL,
  distributor_id BIGINT NOT NULL,
  member_id BIGINT,
  order_id BIGINT,
  amount DECIMAL(12,2) DEFAULT 0,
  commission DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'PENDING',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_report_no UNIQUE (tenant_id, report_no)
);

CREATE TABLE IF NOT EXISTS biz_commission_flow (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  distributor_id BIGINT NOT NULL,
  report_id BIGINT,
  amount DECIMAL(12,2) DEFAULT 0,
  flow_type VARCHAR(16),
  status VARCHAR(32) DEFAULT 'PENDING',
  settled_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS biz_distributor_settlement (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  settlement_no VARCHAR(64) NOT NULL,
  distributor_id BIGINT NOT NULL,
  period VARCHAR(32),
  amount DECIMAL(12,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT 'PENDING',
  paid_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_settlement_no UNIQUE (tenant_id, settlement_no)
);

-- ============ CMS / 官网 ============
CREATE TABLE IF NOT EXISTS cms_article (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  title VARCHAR(256) NOT NULL,
  title_en VARCHAR(256),
  content TEXT,
  content_en TEXT,
  cover VARCHAR(512),
  seo_title VARCHAR(256),
  seo_keywords VARCHAR(256),
  seo_desc VARCHAR(512),
  category VARCHAR(64),
  status VARCHAR(32) DEFAULT 'DRAFT',
  published_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS cms_banner (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  terminal VARCHAR(32) DEFAULT 'PC',
  title VARCHAR(128),
  image VARCHAR(512),
  link VARCHAR(512),
  sort_no INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS cms_form_submission (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  form_type VARCHAR(32),
  name VARCHAR(64),
  phone VARCHAR(64),
  content VARCHAR(1024),
  source VARCHAR(64),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS cms_case (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  title VARCHAR(256),
  title_en VARCHAR(256),
  cover VARCHAR(512),
  content TEXT,
  sort_no INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

-- ============ 微信公众号 11 子页 ============
CREATE TABLE IF NOT EXISTS wx_account (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  wx_id VARCHAR(128),
  app_id VARCHAR(128),
  app_secret VARCHAR(256),
  server_url VARCHAR(256),
  token VARCHAR(128),
  aes_key VARCHAR(256),
  auth_domain VARCHAR(256),
  js_domain VARCHAR(256),
  qr_url VARCHAR(512),
  account_type VARCHAR(32) DEFAULT 'SERVICE',
  verified INT DEFAULT 0,
  api_quota INT DEFAULT 0,
  api_used INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  remark VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_mini_config (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  app_id VARCHAR(128),
  app_secret VARCHAR(256),
  legal_domain VARCHAR(256),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_wx_mini_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS wx_follower (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  openid VARCHAR(128) NOT NULL,
  unionid VARCHAR(128),
  nickname VARCHAR(128),
  avatar VARCHAR(512),
  gender VARCHAR(16),
  city VARCHAR(64),
  province VARCHAR(64),
  subscribed INT DEFAULT 1,
  tags VARCHAR(512),
  subscribed_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT uk_follower_openid UNIQUE (tenant_id, openid)
);

CREATE TABLE IF NOT EXISTS wx_tag (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  wx_tag_id BIGINT,
  name VARCHAR(64),
  count INT DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_message (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  direction VARCHAR(16),
  msg_type VARCHAR(32),
  openid VARCHAR(128),
  content TEXT,
  err_code INT,
  err_msg VARCHAR(512),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_template (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  wx_template_id VARCHAR(128),
  title VARCHAR(128),
  industry VARCHAR(64),
  content TEXT,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_menu_publish (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  menu_json TEXT,
  status VARCHAR(32),
  wx_resp VARCHAR(512),
  published_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_autoreply (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  reply_type VARCHAR(32),
  keyword VARCHAR(128),
  msg_type VARCHAR(32),
  content TEXT,
  media_id VARCHAR(128),
  enabled INT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_material (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  material_type VARCHAR(16),
  name VARCHAR(128),
  url VARCHAR(512),
  thumb_url VARCHAR(512),
  media_id VARCHAR(128),
  size BIGINT DEFAULT 0,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_draft (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  title VARCHAR(256),
  author VARCHAR(64),
  digest VARCHAR(512),
  cover_url VARCHAR(512),
  content TEXT,
  multi_json TEXT,
  status VARCHAR(32) DEFAULT 'DRAFT',
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS wx_publish_record (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  draft_id BIGINT,
  media_id VARCHAR(128),
  status VARCHAR(32) DEFAULT 'SUCCESS',
  fail_reason VARCHAR(512),
  published_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME
);
