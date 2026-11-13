<script>
import { api } from './api/request'

export default {
  onLaunch(options) {
    this.captureSource(options)
    this.syncTabBar()
  },
  methods: {
    captureSource(options) {
      const query = options?.query || {}
      let source = query.source || query.ref || query.utm_source || ''
      // #ifdef H5
      if (!source && typeof window !== 'undefined') {
        const params = new URLSearchParams(window.location.search)
        source = params.get('source') || params.get('ref') || params.get('utm_source') || ''
      }
      // #endif
      if (source) {
        uni.setStorageSync('shareSource', source)
      }
    },
    async syncTabBar() {
      try {
        const config = await api.menu('MINI_TABBAR')
        if (!config || !config.treeJson) return
        const items = JSON.parse(config.treeJson)
        if (!Array.isArray(items)) return
        items.slice(0, 5).forEach((item, index) => {
          if (item.text) {
            uni.setTabBarItem({ index, text: item.text })
          }
        })
      } catch (e) {
        // 后台未配置菜单时使用本地默认 tabBar
      }
    }
  }
}
</script>

<style>
page {
  background: #f5f7fa;
  color: #1f2d3d;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}
.container {
  padding: 24rpx;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 45, 61, 0.05);
}
.section-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
  color: #25324a;
}
.muted {
  color: #8a94a6;
  font-size: 24rpx;
}
.price {
  color: #ff5c8a;
  font-weight: 600;
}
.btn-primary {
  background: linear-gradient(135deg, #ff8a65, #ff5c8a);
  color: #fff;
  border-radius: 999rpx;
  font-size: 30rpx;
  padding: 22rpx 0;
  text-align: center;
}
.btn-ghost {
  border: 1rpx solid #ff5c8a;
  color: #ff5c8a;
  border-radius: 999rpx;
  font-size: 28rpx;
  padding: 18rpx 0;
  text-align: center;
  background: #fff;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tag {
  display: inline-block;
  background: #fff1f5;
  color: #ff5c8a;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  margin-right: 10rpx;
}
.empty {
  text-align: center;
  color: #a2acbd;
  font-size: 26rpx;
  padding: 60rpx 0;
}
</style>
