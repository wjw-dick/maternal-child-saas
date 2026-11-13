-- 初始化数据（平台 + 全局字典 + 演示租户）
INSERT INTO platform_plan (id,code,name,price,billing_cycle,end_count,seat_limit,mp_account_limit,storage_quota_gb,sms_quota,api_quota,trial_days,status,features,deleted,created_at,updated_at) VALUES
(1,'BASIC','基础版',1999.00,'MONTH',4,5,1,10,1000,10000,14,'ACTIVE','PC后台,小程序,H5,公众号',0,NOW(),NOW()),
(2,'STANDARD','标准版',4999.00,'MONTH',6,20,3,50,5000,50000,14,'ACTIVE','全部后台,小程序,H5,公众号,一线端,员工端',0,NOW(),NOW()),
(3,'FLAGSHIP','旗舰版',9999.00,'MONTH',10,100,10,200,20000,200000,14,'ACTIVE','全部端,供应商端,渠道商端,App,开放API',0,NOW(),NOW());

INSERT INTO platform_tenant (id,tenant_code,name,industry,contact,phone,plan_id,status,trial_end_at,expire_at,seat_limit,mp_account_limit,storage_quota_gb,sms_quota,api_quota,remark,deleted,created_at,updated_at) VALUES
(1,'demo','悦宝母婴护理中心','母婴护理','张店长','13800000000',2,'ACTIVE','2026-12-31 23:59:59','2028-12-31 23:59:59',20,3,50,5000,50000,'演示租户',0,NOW(),NOW());

INSERT INTO platform_tenant_quota_usage (id,tenant_id,used_seats,used_mp,used_storage_mb,used_sms,used_api,stat_date,deleted,created_at,updated_at) VALUES
(1,1,4,1,12,120,860,CURRENT_DATE,0,NOW(),NOW());

INSERT INTO sys_role (id,tenant_id,name,code,data_scope,remark,deleted,created_at,updated_at) VALUES
(1,0,'平台超管','PLATFORM_ADMIN','ALL','平台全部权限',0,NOW(),NOW()),
(2,0,'平台运营','PLATFORM_OPER','ALL','租户运营',0,NOW(),NOW()),
(3,0,'平台财务','PLATFORM_FINANCE','ALL','账单财务',0,NOW(),NOW()),
(4,0,'租户管理员','TENANT_ADMIN','ALL','租户全部权限',0,NOW(),NOW()),
(5,0,'店长','STORE_MANAGER','STORE','门店管理',0,NOW(),NOW()),
(6,0,'前台','FRONT_DESK','STORE','预约核销',0,NOW(),NOW()),
(7,0,'护理主管','CARE_MANAGER','STORE','护理调度',0,NOW(),NOW()),
(8,0,'护理师','NURSE','SELF','一线服务',0,NOW(),NOW()),
(9,0,'育儿师','NANNY','SELF','一线服务',0,NOW(),NOW()),
(10,0,'康复师','REHAB','SELF','康复服务',0,NOW(),NOW()),
(11,0,'财务','FINANCE','ALL','财务结算',0,NOW(),NOW()),
(12,0,'库管','WAREHOUSE','STORE','库存管理',0,NOW(),NOW()),
(13,0,'人事','HR','ALL','员工档案',0,NOW(),NOW()),
(14,0,'供应商','SUPPLIER','SELF','供货对账',0,NOW(),NOW()),
(15,0,'渠道商','DISTRIBUTOR','SELF','报单佣金',0,NOW(),NOW()),
(16,0,'会员','MEMBER','SELF','C端会员',0,NOW(),NOW());

INSERT INTO sys_menu (id,tenant_id,parent_id,scope,name,code,menu_type,path,component,icon,sort_no,visible,status,deleted,created_at,updated_at) VALUES
(100,0,0,'PLATFORM','平台看板','platform:dashboard','MENU','/platform/dashboard','platform/dashboard','DataBoard',1,1,'ACTIVE',0,NOW(),NOW()),
(101,0,0,'PLATFORM','租户管理','platform:tenant:read','MENU','/platform/tenants','platform/tenants','OfficeBuilding',2,1,'ACTIVE',0,NOW(),NOW()),
(102,0,0,'PLATFORM','套餐配额','platform:plan:read','MENU','/platform/plans','platform/plans','Tickets',3,1,'ACTIVE',0,NOW(),NOW()),
(103,0,0,'PLATFORM','账单计费','platform:bill:read','MENU','/platform/bills','platform/bills','Money',4,1,'ACTIVE',0,NOW(),NOW()),
(104,0,0,'PLATFORM','平台公告','platform:notice:read','MENU','/platform/notices','platform/notices','Bell',5,1,'ACTIVE',0,NOW(),NOW()),
(105,0,0,'PLATFORM','平台审计','platform:audit:read','MENU','/platform/audit','platform/audit','Document',6,1,'ACTIVE',0,NOW(),NOW()),
(200,0,0,'TENANT','工作台','dashboard:read','MENU','/dashboard','dashboard','Odometer',1,1,'ACTIVE',0,NOW(),NOW()),
(210,0,0,'TENANT','会员管理',NULL,'DIR','/member',NULL,'User',2,1,'ACTIVE',0,NOW(),NOW()),
(211,0,210,'TENANT','家庭会员','member:read','MENU','/member/list','member/list','UserFilled',1,1,'ACTIVE',0,NOW(),NOW()),
(212,0,210,'TENANT','宝宝档案','baby:read','MENU','/member/babies','member/babies','Avatar',2,1,'ACTIVE',0,NOW(),NOW()),
(220,0,0,'TENANT','预约服务',NULL,'DIR','/appointment',NULL,'Calendar',3,1,'ACTIVE',0,NOW(),NOW()),
(221,0,220,'TENANT','预约管理','appointment:read','MENU','/appointment/list','appointment/list','Calendar',1,1,'ACTIVE',0,NOW(),NOW()),
(230,0,0,'TENANT','商品订单',NULL,'DIR','/trade',NULL,'Goods',4,1,'ACTIVE',0,NOW(),NOW()),
(231,0,230,'TENANT','商品管理','product:read','MENU','/trade/products','trade/products','Goods',1,1,'ACTIVE',0,NOW(),NOW()),
(232,0,230,'TENANT','订单管理','order:read','MENU','/trade/orders','trade/orders','List',2,1,'ACTIVE',0,NOW(),NOW()),
(240,0,0,'TENANT','门店服务',NULL,'DIR','/store',NULL,'Shop',5,1,'ACTIVE',0,NOW(),NOW()),
(241,0,240,'TENANT','门店档案','store:read','MENU','/store/list','store/list','Shop',1,1,'ACTIVE',0,NOW(),NOW()),
(242,0,240,'TENANT','服务项目','service:read','MENU','/store/services','store/services','Service',2,1,'ACTIVE',0,NOW(),NOW()),
(243,0,240,'TENANT','护理排班','schedule:read','MENU','/store/schedules','store/schedules','Clock',3,1,'ACTIVE',0,NOW(),NOW()),
(250,0,0,'TENANT','储值营销',NULL,'DIR','/marketing',NULL,'Present',6,1,'ACTIVE',0,NOW(),NOW()),
(251,0,250,'TENANT','卡券管理','coupon:read','MENU','/marketing/coupons','marketing/coupons','Ticket',1,1,'ACTIVE',0,NOW(),NOW()),
(252,0,250,'TENANT','亲子活动','activity:read','MENU','/marketing/activities','marketing/activities','Flag',2,1,'ACTIVE',0,NOW(),NOW()),
(260,0,0,'TENANT','供应链',NULL,'DIR','/supply',NULL,'Box',7,1,'ACTIVE',0,NOW(),NOW()),
(261,0,260,'TENANT','供应商','supplier:read','MENU','/supply/suppliers','supply/suppliers','Truck',1,1,'ACTIVE',0,NOW(),NOW()),
(262,0,260,'TENANT','供货单','supply:read','MENU','/supply/orders','supply/orders','Document',2,1,'ACTIVE',0,NOW(),NOW()),
(270,0,0,'TENANT','渠道分销',NULL,'DIR','/channel',NULL,'Share',8,1,'ACTIVE',0,NOW(),NOW()),
(271,0,270,'TENANT','渠道商','distributor:read','MENU','/channel/list','channel/list','User',1,1,'ACTIVE',0,NOW(),NOW()),
(272,0,270,'TENANT','渠道报单','report:read','MENU','/channel/reports','channel/reports','Tickets',2,1,'ACTIVE',0,NOW(),NOW()),
(280,0,0,'TENANT','员工管理',NULL,'DIR','/staff',NULL,'Team',9,1,'ACTIVE',0,NOW(),NOW()),
(281,0,280,'TENANT','员工档案','staff:read','MENU','/staff/list','staff/list','User',1,1,'ACTIVE',0,NOW(),NOW()),
(282,0,280,'TENANT','资质证照','license:read','MENU','/staff/licenses','staff/licenses','Postcard',2,1,'ACTIVE',0,NOW(),NOW()),
(290,0,0,'TENANT','公众号',NULL,'DIR','/wechat',NULL,'ChatDotRound',10,1,'ACTIVE',0,NOW(),NOW()),
(291,0,290,'TENANT','账号管理','wx:account:read','MENU','/wechat/account','wechat/account','ChatDotRound',1,1,'ACTIVE',0,NOW(),NOW()),
(292,0,290,'TENANT','粉丝与标签','wx:follower:read','MENU','/wechat/followers','wechat/followers','User',2,1,'ACTIVE',0,NOW(),NOW()),
(293,0,290,'TENANT','素材与草稿','wx:material:read','MENU','/wechat/materials','wechat/materials','Picture',3,1,'ACTIVE',0,NOW(),NOW()),
(294,0,290,'TENANT','菜单发布','wx:menu:read','MENU','/wechat/menu','wechat/menu','Menu',4,1,'ACTIVE',0,NOW(),NOW()),
(300,0,0,'TENANT','系统设置',NULL,'DIR','/system',NULL,'Setting',11,1,'ACTIVE',0,NOW(),NOW()),
(301,0,300,'TENANT','全局参数','config:read','MENU','/system/config','system/config','Setting',1,1,'ACTIVE',0,NOW(),NOW()),
(302,0,300,'TENANT','菜单可视化','menu:read','MENU','/system/menus','system/menus','Menu',2,1,'ACTIVE',0,NOW(),NOW()),
(303,0,300,'TENANT','对象存储','storage:read','MENU','/system/storage','system/storage','Folder',3,1,'ACTIVE',0,NOW(),NOW()),
(304,0,300,'TENANT','消息配置','message:read','MENU','/system/message','system/message','Message',4,1,'ACTIVE',0,NOW(),NOW()),
(305,0,300,'TENANT','SSO 配置','sso:read','MENU','/system/sso','system/sso','Link',5,1,'ACTIVE',0,NOW(),NOW()),
(306,0,300,'TENANT','角色权限','role:read','MENU','/system/roles','system/roles','Lock',6,1,'ACTIVE',0,NOW(),NOW()),
(307,0,300,'TENANT','账号管理','user:read','MENU','/system/users','system/users','User',7,1,'ACTIVE',0,NOW(),NOW()),
(308,0,300,'TENANT','审计日志','audit:read','MENU','/system/audit','system/audit','Document',8,1,'ACTIVE',0,NOW(),NOW()),
(309,0,300,'TENANT','多语言','i18n:read','MENU','/system/i18n','system/i18n','Language',9,1,'ACTIVE',0,NOW(),NOW()),
(310,0,0,'TENANT','经营报表','report:read','MENU','/report','report','TrendCharts',12,1,'ACTIVE',0,NOW(),NOW()),
(320,0,0,'TENANT','公众号数据','wx:stats:read','MENU','/wechat/stats','wechat/stats','DataLine',13,1,'ACTIVE',0,NOW(),NOW()),
(321,0,0,'TENANT','公众号消息','wx:message:read','MENU','/wechat/messages','wechat/messages','Message',14,1,'ACTIVE',0,NOW(),NOW()),
(322,0,0,'TENANT','公众号模版','wx:template:read','MENU','/wechat/templates','wechat/templates','Document',15,1,'ACTIVE',0,NOW(),NOW()),
(323,0,0,'TENANT','自动回复','wx:autoreply:read','MENU','/wechat/autoreply','wechat/autoreply','ChatLineRound',16,1,'ACTIVE',0,NOW(),NOW()),
(324,0,0,'TENANT','图文发表','wx:publish:read','MENU','/wechat/publish','wechat/publish','Promotion',17,1,'ACTIVE',0,NOW(),NOW());

INSERT INTO sys_config (id,tenant_id,config_key,config_value,group_code,encrypted,remark,deleted,created_at,updated_at) VALUES
(1,0,'site_name','悦宝母婴','site',0,'站点名称',0,NOW(),NOW()),
(2,0,'site_logo','/files/logo.png','site',0,'站点LOGO',0,NOW(),NOW()),
(3,0,'icp','粤ICP备00000000号','site',0,'ICP备案号',0,NOW(),NOW()),
(4,0,'service_phone','400-000-0000','site',0,'客服电话',0,NOW(),NOW()),
(5,0,'business_hours','09:00-21:00','site',0,'营业时间',0,NOW(),NOW()),
(6,0,'seo_title','悦宝母婴护理 - 专业母婴亲子服务','seo',0,'SEO标题',0,NOW(),NOW()),
(7,0,'seo_keywords','母婴护理,育儿,产后康复','seo',0,'SEO关键词',0,NOW(),NOW()),
(8,0,'seo_desc','专业母婴护理与亲子服务平台','seo',0,'SEO描述',0,NOW(),NOW()),
(9,0,'i18n_enabled','true','i18n',0,'多语言开关',0,NOW(),NOW()),
(10,0,'wechat_login_enabled','true','wechat',0,'微信登录开关',0,NOW(),NOW()),
(11,0,'pay_wechat_enabled','true','pay',0,'微信支付开关',0,NOW(),NOW()),
(12,0,'pay_alipay_enabled','true','pay',0,'支付宝开关',0,NOW(),NOW()),
(13,0,'sms_enabled','false','sms',0,'短信开关',0,NOW(),NOW()),
(14,0,'privacy_version','2026-01','compliance',0,'隐私协议版本',0,NOW(),NOW()),
(15,0,'sensitive_words','代孕,违禁药品','security',0,'敏感词',0,NOW(),NOW());

INSERT INTO sys_language (id,tenant_id,code,name,enabled,is_default,sort_no,deleted,created_at,updated_at) VALUES
(1,0,'zh-CN','简体中文',1,1,1,0,NOW(),NOW()),
(2,0,'en-US','English',1,0,2,0,NOW(),NOW());

INSERT INTO sys_i18n (id,tenant_id,lang_code,i18n_key,i18n_value,deleted,created_at,updated_at) VALUES
(1,0,'zh-CN','home.title','专业母婴护理',0,NOW(),NOW()),
(2,0,'en-US','home.title','Professional Maternal Care',0,NOW(),NOW()),
(3,0,'zh-CN','common.appointment','立即预约',0,NOW(),NOW()),
(4,0,'en-US','common.appointment','Book Now',0,NOW(),NOW());

INSERT INTO sys_dict_type (id,tenant_id,dict_code,dict_name,scope,deleted,created_at,updated_at) VALUES
(1,0,'service_category','服务分类','GLOBAL',0,NOW(),NOW()),
(2,0,'product_category','商品分类','GLOBAL',0,NOW(),NOW()),
(3,0,'age_range','适用年龄段','GLOBAL',0,NOW(),NOW()),
(4,0,'order_status','订单状态','GLOBAL',0,NOW(),NOW()),
(5,0,'pay_status','支付状态','GLOBAL',0,NOW(),NOW()),
(6,0,'license_type','证照类型','GLOBAL',0,NOW(),NOW()),
(7,0,'commission_rule','佣金规则类型','GLOBAL',0,NOW(),NOW());

INSERT INTO sys_dict_item (id,tenant_id,dict_code,item_value,item_label,sort_no,status,deleted,created_at,updated_at) VALUES
(1,0,'service_category','CARE','母婴护理',1,1,0,NOW(),NOW()),
(2,0,'service_category','NANNY','育儿照护',2,1,0,NOW(),NOW()),
(3,0,'service_category','REHAB','产后康复',3,1,0,NOW(),NOW()),
(4,0,'product_category','FOOD','食品辅食',1,1,0,NOW(),NOW()),
(5,0,'product_category','DAILY','日用洗护',2,1,0,NOW(),NOW()),
(6,0,'product_category','TOY','玩具早教',3,1,0,NOW(),NOW()),
(7,0,'age_range','0-6M','0-6个月',1,1,0,NOW(),NOW()),
(8,0,'age_range','6-12M','6-12个月',2,1,0,NOW(),NOW()),
(9,0,'age_range','1-3Y','1-3岁',3,1,0,NOW(),NOW()),
(10,0,'age_range','3-6Y','3-6岁',4,1,0,NOW(),NOW()),
(11,0,'order_status','PENDING','待支付',1,1,0,NOW(),NOW()),
(12,0,'order_status','PAID','已支付',2,1,0,NOW(),NOW()),
(13,0,'order_status','COMPLETED','已完成',3,1,0,NOW(),NOW()),
(14,0,'pay_status','UNPAID','未支付',1,1,0,NOW(),NOW()),
(15,0,'pay_status','PAID','已支付',2,1,0,NOW(),NOW()),
(16,0,'license_type','HEALTH','健康证',1,1,0,NOW(),NOW()),
(17,0,'license_type','NURSE','母婴护理证',2,1,0,NOW(),NOW()),
(18,0,'commission_rule','PERCENT','按比例',1,1,0,NOW(),NOW()),
(19,0,'commission_rule','FIXED','按固定额',2,1,0,NOW(),NOW());

INSERT INTO biz_store (id,tenant_id,code,name,address,phone,business_hours,service_range,delivery_range,lng,lat,status,deleted,created_at,updated_at) VALUES
(1001,1,'S001','悦宝母婴·天河旗舰店','广州市天河区体育西路 1 号','020-88886666','09:00-21:00','到店护理,上门护理','5公里',113.321000,23.135000,'ACTIVE',0,NOW(),NOW()),
(1002,1,'S002','悦宝母婴·海珠店','广州市海珠区江南大道 88 号','020-88887777','09:00-20:00','到店护理','3公里',113.269000,23.090000,'ACTIVE',0,NOW(),NOW());

INSERT INTO biz_service (id,tenant_id,code,name,category,price,duration,age_range,cover,description,status,deleted,created_at,updated_at) VALUES
(1101,1,'SV001','新生儿居家护理','CARE',399.00,120,'0-6M','/files/demo/service1.jpg','专业护理师上门新生儿护理','ACTIVE',0,NOW(),NOW()),
(1102,1,'SV002','产后康复调理','REHAB',599.00,90,'ADULT','/files/demo/service2.jpg','产后体质评估与康复','ACTIVE',0,NOW(),NOW()),
(1103,1,'SV003','育儿嫂半日服务','NANNY',299.00,240,'6-36M','/files/demo/service3.jpg','育儿嫂半日居家照护','ACTIVE',0,NOW(),NOW());

INSERT INTO biz_staff (id,tenant_id,user_id,store_id,name,role_type,phone,level,status,deleted,created_at,updated_at) VALUES
(1201,1,NULL,1001,'李护理','NURSE','13900000001','高级','ACTIVE',0,NOW(),NOW()),
(1202,1,NULL,1001,'王育儿','NANNY','13900000002','中级','ACTIVE',0,NOW(),NOW()),
(1203,1,NULL,1002,'陈康复','REHAB','13900000003','高级','ACTIVE',0,NOW(),NOW());

INSERT INTO biz_schedule (id,tenant_id,staff_id,store_id,work_date,time_slot,capacity,booked,status,deleted,created_at,updated_at) VALUES
(1251,1,1201,1001,CURRENT_DATE,'09:00-12:00',2,1,'OPEN',0,NOW(),NOW()),
(1252,1,1202,1001,CURRENT_DATE,'14:00-18:00',2,0,'OPEN',0,NOW(),NOW()),
(1253,1,1203,1002,CURRENT_DATE,'09:00-18:00',1,0,'OPEN',0,NOW(),NOW());

INSERT INTO biz_product (id,tenant_id,spu_code,name,category,brand,age_range,cover,images,detail,status,deleted,created_at,updated_at) VALUES
(1301,1,'SP001','有机婴幼儿米粉','FOOD','悦宝','6-12M','/files/demo/product1.jpg','[]','有机高铁米粉 250g','ACTIVE',0,NOW(),NOW()),
(1302,1,'SP002','婴儿抚触油','DAILY','悦宝','0-6M','/files/demo/product2.jpg','[]','天然植物抚触油 100ml','ACTIVE',0,NOW(),NOW()),
(1303,1,'SP003','早教布书套装','TOY','悦宝','6-12M','/files/demo/product3.jpg','[]','可啃咬益智布书 4 册','ACTIVE',0,NOW(),NOW());

INSERT INTO biz_sku (id,tenant_id,product_id,sku_code,spec_json,price,cost_price,stock,safety_stock,supplier_id,status,deleted,created_at,updated_at) VALUES
(1401,1,1301,'SK001','{\"规格\":\"250g\"}',68.00,42.00,120,20,5001,'ACTIVE',0,NOW(),NOW()),
(1402,1,1302,'SK002','{\"规格\":\"100ml\"}',59.00,30.00,80,20,5001,'ACTIVE',0,NOW(),NOW()),
(1403,1,1303,'SK003','{\"规格\":\"4册\"}',89.00,45.00,60,10,5001,'ACTIVE',0,NOW(),NOW());

INSERT INTO biz_member (id,tenant_id,member_no,name,phone,phone_enc,phone_hash,id_card,id_card_enc,gender,birthday,level,balance,points,tags,source,openid,unionid,status,remark,deleted,created_at,updated_at) VALUES
(2001,1,'M20260001','张晓雨','138****0001',NULL,NULL,NULL,NULL,'F','1995-06-12','GOLD',500.00,320,'产后康复,高频','MINI','openid_demo_1','unionid_demo_1','ACTIVE','演示会员',0,NOW(),NOW()),
(2002,1,'M20260002','刘子涵','139****0002',NULL,NULL,NULL,NULL,'M','1992-03-08','NORMAL',0.00,80,'新生儿','STORE',NULL,NULL,'ACTIVE','演示会员',0,NOW(),NOW());

INSERT INTO biz_baby (id,tenant_id,member_id,name,gender,birthday,allergy,health_note,avatar,deleted,created_at,updated_at) VALUES
(2101,1,2001,'小葡萄','F','2025-11-20','无','足月顺产，体重 3.2kg',NULL,0,NOW(),NOW()),
(2102,1,2002,'小年糕','M','2026-02-14','蛋白过敏','需关注湿疹',NULL,0,NOW(),NOW());

INSERT INTO biz_wallet (id,tenant_id,member_id,balance,total_recharge,total_consume,deleted,created_at,updated_at) VALUES
(2201,1,2001,500.00,1000.00,500.00,0,NOW(),NOW());

INSERT INTO biz_wallet_flow (id,tenant_id,member_id,flow_no,flow_type,amount,balance,biz_no,remark,deleted,created_at,updated_at) VALUES
(2251,1,2001,'WF20260001','RECHARGE',1000.00,1000.00,'PAY_DEMO','开卡充值',0,NOW(),NOW()),
(2252,1,2001,'WF20260002','CONSUME',-500.00,500.00,'ORD_DEMO','服务消费',0,NOW(),NOW());

INSERT INTO biz_order (id,tenant_id,order_no,member_id,store_id,order_type,total_amount,discount_amount,pay_amount,pay_status,status,pay_channel,pay_time,remark,source,idempotency_key,deleted,created_at,updated_at) VALUES
(3001,1,'ORD20260001',2001,1001,'PRODUCT',127.00,0.00,127.00,'PAID','COMPLETED','WECHAT','2026-11-01 10:20:00','演示订单','MINI','seed-1',0,NOW(),NOW());

INSERT INTO biz_order_item (id,tenant_id,order_id,item_type,item_id,item_name,spec,price,qty,amount,deleted,created_at,updated_at) VALUES
(3051,1,3001,'SKU',1402,'婴儿抚触油','100ml',59.00,1,59.00,0,NOW(),NOW()),
(3052,1,3001,'SKU',1401,'有机婴幼儿米粉','250g',68.00,1,68.00,0,NOW(),NOW());

INSERT INTO biz_payment (id,tenant_id,pay_no,order_id,channel,amount,status,trade_no,paid_at,raw_json,deleted,created_at,updated_at) VALUES
(3100,1,'PAY20260001',3001,'WECHAT',127.00,'SUCCESS','WX_DEMO_001','2026-11-01 10:20:00',NULL,0,NOW(),NOW());

INSERT INTO biz_appointment (id,tenant_id,appointment_no,member_id,baby_id,store_id,service_id,staff_id,appoint_date,time_slot,appoint_type,address,contact,phone,status,verify_code,remark,deleted,created_at,updated_at) VALUES
(3201,1,'AP20260001',2001,2101,1001,1101,1201,CURRENT_DATE,'09:00-12:00','HOME','广州市天河区珠江新城 8 号','张晓雨','13800000001','CONFIRMED','836521','首次上门',0,NOW(),NOW());

INSERT INTO biz_dispatch (id,tenant_id,appointment_id,staff_id,status,accepted_at,deleted,created_at,updated_at) VALUES
(3251,1,3201,1201,'ACCEPTED',NOW(),0,NOW(),NOW());

INSERT INTO biz_coupon (id,tenant_id,name,coupon_type,face_value,min_amount,total,issued,valid_from,valid_to,status,deleted,created_at,updated_at) VALUES
(4001,1,'新客立减 50 元','CASH',50.00,199.00,1000,120,'2026-01-01','2027-12-31','ACTIVE',0,NOW(),NOW()),
(4002,1,'服务 9 折券','DISCOUNT',0.90,0.00,500,60,'2026-01-01','2027-12-31','ACTIVE',0,NOW(),NOW());

INSERT INTO biz_member_coupon (id,tenant_id,coupon_id,member_id,status,used_at,order_id,deleted,created_at,updated_at) VALUES
(4051,1,4001,2001,'UNUSED',NULL,NULL,0,NOW(),NOW()),
(4052,1,4002,2001,'UNUSED',NULL,NULL,0,NOW(),NOW());

INSERT INTO biz_activity (id,tenant_id,title,cover,start_at,end_at,address,capacity,joined,price,status,content,deleted,created_at,updated_at) VALUES
(4101,1,'亲子抚触体验课','/files/demo/activity1.jpg','2026-12-05 10:00:00','2026-12-05 11:30:00','天河旗舰店',20,8,0.00,'ACTIVE','专业护理师带教新生儿抚触',0,NOW(),NOW());

INSERT INTO biz_supplier (id,tenant_id,code,name,contact,phone,address,agreement_no,rating,status,deleted,created_at,updated_at) VALUES
(5001,1,'SUP001','广州优品母婴供应链','林经理','13700000001','广州市白云区物流园 3 栋','HT2026001','A','ACTIVE',0,NOW(),NOW());

INSERT INTO biz_supply_order (id,tenant_id,supply_no,supplier_id,store_id,total_amount,status,expected_at,signed_at,deleted,created_at,updated_at) VALUES
(5101,1,'SO20260001',5001,1001,2520.00,'PENDING','2026-11-20 10:00:00',NULL,0,NOW(),NOW());

INSERT INTO biz_supply_item (id,tenant_id,supply_id,sku_id,name,qty,price,amount,deleted,created_at,updated_at) VALUES
(5151,1,5101,1401,'有机婴幼儿米粉',30,42.00,1260.00,0,NOW(),NOW()),
(5152,1,5101,1402,'婴儿抚触油',30,42.00,1260.00,0,NOW(),NOW());

INSERT INTO biz_distributor (id,tenant_id,code,name,contact,phone,parent_id,commission_rule,credit_limit,used_credit,status,deleted,created_at,updated_at) VALUES
(6001,1,'D001','宝妈联盟渠道','周女士','13600000001',0,'PERCENT:15',50000.00,1200.00,'ACTIVE',0,NOW(),NOW());

INSERT INTO biz_distributor_report (id,tenant_id,report_no,distributor_id,member_id,order_id,amount,commission,status,deleted,created_at,updated_at) VALUES
(6101,1,'RP20260001',6001,2001,3001,399.00,59.85,'APPROVED',0,NOW(),NOW());

INSERT INTO biz_commission_flow (id,tenant_id,distributor_id,report_id,amount,flow_type,status,settled_at,deleted,created_at,updated_at) VALUES
(6151,1,6001,6101,59.85,'COMMISSION','PENDING',NULL,0,NOW(),NOW());

INSERT INTO biz_supplier_statement (id,tenant_id,supplier_id,period,amount,status,deleted,created_at,updated_at) VALUES
(5201,1,5001,'2026-10',2520.00,'SETTLED',0,NOW(),NOW()),
(5202,1,5001,'2026-11',1260.00,'UNSETTLED',0,NOW(),NOW());

INSERT INTO biz_supplier_qualification (id,tenant_id,supplier_id,license_type,license_no,file_url,expire_date,audit_status,remark,deleted,created_at,updated_at) VALUES
(5251,1,5001,'食品经营许可证','JY2026001','/files/demo/supplier-license.jpg','2028-12-31','APPROVED','年度审核通过',0,NOW(),NOW());

INSERT INTO biz_distributor (id,tenant_id,code,name,contact,phone,parent_id,commission_rule,credit_limit,used_credit,status,deleted,created_at,updated_at) VALUES
(6002,1,'D002','番禺母婴渠道','陈先生','13600000002',6001,'PERCENT:10',20000.00,0.00,'ACTIVE',0,NOW(),NOW());

INSERT INTO biz_distributor_settlement (id,tenant_id,settlement_no,distributor_id,period,amount,status,paid_at,deleted,created_at,updated_at) VALUES
(6201,1,'ST20260001',6001,'2026-10',59.85,'PAID','2026-11-10 10:00:00',0,NOW(),NOW()),
(6202,1,'ST20260002',6001,'2026-11',120.00,'PENDING',NULL,0,NOW(),NOW());

INSERT INTO platform_notice (id,title,content,target_type,target_tenant_ids,status,published_at,deleted,created_at,updated_at) VALUES
(8001,'平台服务时间调整通知','国庆期间客服值班时间调整为 9:00-18:00，紧急工单请拨打值班电话。','ALL',NULL,'PUBLISHED','2026-11-01 09:00:00',0,NOW(),NOW()),
(8002,'供应商结算周期说明','供应商对账单每月 5 日生成，结算周期为 T+7，请及时确认。','ALL',NULL,'PUBLISHED','2026-11-05 09:00:00',0,NOW(),NOW());

INSERT INTO cms_article (id,tenant_id,title,title_en,content,content_en,cover,seo_title,seo_keywords,seo_desc,category,status,published_at,deleted,created_at,updated_at) VALUES
(7001,1,'新手爸妈必读：新生儿护理 10 个细节','Newborn Care: 10 Details', '<p>新生儿护理要点……</p>','<p>Newborn care tips...</p>','/files/demo/news1.jpg','新生儿护理 10 个细节','新生儿护理,育儿','新手爸妈必读','NEWS','PUBLISHED','2026-11-01 09:00:00',0,NOW(),NOW());

INSERT INTO cms_banner (id,tenant_id,terminal,title,image,link,sort_no,status,deleted,created_at,updated_at) VALUES
(7101,1,'PC','专业母婴护理','/files/demo/banner1.jpg','/services',1,'ACTIVE',0,NOW(),NOW()),
(7102,1,'MINI','新生儿护理上门','/files/demo/banner2.jpg','/pages/service/list',1,'ACTIVE',0,NOW(),NOW());

INSERT INTO cms_case (id,tenant_id,title,title_en,cover,content,sort_no,status,deleted,created_at,updated_at) VALUES
(7201,1,'双胞胎家庭护理案例','Twin Family Care Case','/files/demo/case1.jpg','<p>28 天全程护理……</p>',1,'ACTIVE',0,NOW(),NOW());

INSERT INTO wx_account (id,tenant_id,name,wx_id,app_id,app_secret,server_url,token,aes_key,auth_domain,js_domain,qr_url,account_type,verified,api_quota,api_used,status,remark,deleted,created_at,updated_at) VALUES
(8001,1,'悦宝母婴服务号','yuebao_wx','wx_demo_appid','','https://demo.quantaida.cloud/api/wx/callback','demo_token','','demo.quantaida.cloud','demo.quantaida.cloud','/files/demo/qr.png','SERVICE',1,100000,3200,'ACTIVE','演示公众号（未接真实凭证）',0,NOW(),NOW());

INSERT INTO wx_mini_config (id,tenant_id,app_id,app_secret,legal_domain,deleted,created_at,updated_at) VALUES
(8051,1,'wx_demo_mini','','demo.quantaida.cloud',0,NOW(),NOW());

INSERT INTO wx_follower (id,tenant_id,openid,unionid,nickname,avatar,gender,city,province,subscribed,tags,subscribed_at,deleted,created_at,updated_at) VALUES
(8101,1,'openid_demo_1','unionid_demo_1','小葡萄妈妈','', 'F','广州','广东',1,'产后康复',NOW(),0,NOW(),NOW()),
(8102,1,'openid_demo_2','unionid_demo_2','年糕爸爸','', 'M','广州','广东',1,'新生儿',NOW(),0,NOW(),NOW());

INSERT INTO wx_tag (id,tenant_id,wx_tag_id,name,count,deleted,created_at,updated_at) VALUES
(8151,1,101,'产后康复',1,0,NOW(),NOW()),
(8152,1,102,'新生儿',1,0,NOW(),NOW());

INSERT INTO wx_message (id,tenant_id,direction,msg_type,openid,content,err_code,err_msg,deleted,created_at,updated_at) VALUES
(8201,1,'IN','TEXT','openid_demo_1','我想预约新生儿护理',0,'ok',0,NOW(),NOW()),
(8202,1,'OUT','TEMPLATE','openid_demo_1','预约成功通知',0,'ok',0,NOW(),NOW());

INSERT INTO wx_template (id,tenant_id,wx_template_id,title,industry,content,deleted,created_at,updated_at) VALUES
(8251,1,'TPL_DEMO_001','预约成功通知','母婴护理','{"thing1":"服务项目","time2":"预约时间","phrase3":"预约状态"}',0,NOW(),NOW());

INSERT INTO wx_autoreply (id,tenant_id,reply_type,keyword,msg_type,content,media_id,enabled,deleted,created_at,updated_at) VALUES
(8301,1,'SUBSCRIBE',NULL,'TEXT','欢迎关注悦宝母婴，回复“预约”获取服务。',NULL,1,0,NOW(),NOW()),
(8302,1,'KEYWORD','预约','TEXT','点击菜单“服务预约”即可在线预约。',NULL,1,0,NOW(),NOW());

INSERT INTO wx_material (id,tenant_id,material_type,name,url,thumb_url,media_id,size,deleted,created_at,updated_at) VALUES
(8401,1,'IMAGE','品牌主图','/files/demo/material1.jpg',NULL,'MEDIA_DEMO_1',102400,0,NOW(),NOW());

INSERT INTO wx_draft (id,tenant_id,title,author,digest,cover_url,content,multi_json,status,deleted,created_at,updated_at) VALUES
(8501,1,'冬季新生儿护理指南','悦宝','冬季护理要点','/files/demo/news1.jpg','<p>冬季护理要点……</p>',NULL,'DRAFT',0,NOW(),NOW());

INSERT INTO wx_publish_record (id,tenant_id,draft_id,media_id,status,fail_reason,published_at,deleted,created_at,updated_at) VALUES
(8551,1,8501,'MEDIA_PUB_DEMO','SUCCESS',NULL,NOW(),0,NOW(),NOW());
