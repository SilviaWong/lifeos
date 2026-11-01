<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('LifeOS App Launch')
})

onShow(() => {
  console.log('LifeOS App Show')
})

onHide(() => {
  console.log('LifeOS App Hide')
})
</script>

<style lang="scss">
/* 每个页面公共 css */
page {
  background-color: #f7f8fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto,
    'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
  color: #1f2d3d;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

view,
text,
scroll-view,
input,
button {
  box-sizing: border-box;
}

/* 通用移动端卡片规范 */
.life-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 20rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.life-card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.life-btn-primary {
  background-color: #1677ff;
  color: #ffffff;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.life-btn-primary:active {
  background-color: #0958d9;
}
</style>

