<template>
  <div>
    <section class="page-hero page-hero--brand">
      <div class="container">
        <span class="eyebrow">{{ t('navAbout') }}</span>
        <h1>{{ t('aboutTitle') }}</h1>
        <p>{{ t('aboutDesc') }}</p>
      </div>
    </section>

    <section class="section">
      <div class="container stats-band">
        <div class="stat"><strong>2018</strong><span>Founded</span></div>
        <div class="stat"><strong>120+</strong><span>Certified specialists</span></div>
        <div class="stat"><strong>8,600+</strong><span>Families served</span></div>
        <div class="stat"><strong>4.9 / 5</strong><span>Service rating</span></div>
      </div>
    </section>

    <section class="section section--soft">
      <div class="container">
        <SectionHead :title="t('aboutTitle')" :desc="t('aboutPoint3')" />
        <div class="card-grid card-grid--3">
          <article class="value-card" v-for="item in values" :key="item.title">
            <span class="value-icon">{{ item.icon }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container timeline">
        <div class="timeline-item" v-for="item in timeline" :key="item.year">
          <strong>{{ item.year }}</strong>
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-band">
      <div class="container cta-inner">
        <div>
          <h2>{{ t('contactTitle') }}</h2>
          <p>{{ t('contactDesc') }}</p>
        </div>
        <RouterLink to="/contact" class="primary-btn primary-btn--light">{{ t('bookNow') }}</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import SectionHead from '../components/SectionHead.vue'
import { t } from '../i18n'

const values = [
  { icon: '✦', title: '专业认证', desc: '护理师、育儿师、康复师持证上岗，服务流程标准化、可追溯。' },
  { icon: '◎', title: '一客一档', desc: '宝宝成长、护理记录、过敏与健康信息连续沉淀，换人服务也不断档。' },
  { icon: '♥', title: '温度服务', desc: '不只完成护理动作，更关注新手父母的情绪与真实需求。' }
]

const timeline = [
  { year: '2018', title: '品牌创立', desc: '首家门店落地广州天河，专注新生儿居家护理。' },
  { year: '2021', title: '服务升级', desc: '上线产后康复与育儿嫂服务，建立护理师培训体系。' },
  { year: '2024', title: '数字化平台', desc: '打通小程序、H5、App 与门店系统，会员多端数据互通。' },
  { year: '2026', title: '多城拓展', desc: '以多租户 SaaS 能力支持区域合作伙伴共同服务家庭。' }
]
</script>
