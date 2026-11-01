<template>
  <view class="health-page">
    <view class="life-card">
      <view class="life-card-title">健康数据概览</view>
      <view class="metric-grid">
        <view class="metric-card">
          <text class="metric-name">今日步数</text>
          <text class="metric-num">-- <text class="metric-unit">步</text></text>
          <text class="metric-target">参考: 6000~10000 步</text>
        </view>
        <view class="metric-card">
          <text class="metric-name">昨晚睡眠</text>
          <text class="metric-num">-- <text class="metric-unit">小时</text></text>
          <text class="metric-target">参考: 7~9 小时</text>
        </view>
        <view class="metric-card">
          <text class="metric-name">当前体重</text>
          <text class="metric-num">-- <text class="metric-unit">kg</text></text>
          <text class="metric-target">目标体重跟踪</text>
        </view>
        <view class="metric-card">
          <text class="metric-name">今日饮水</text>
          <text class="metric-num">-- <text class="metric-unit">ml</text></text>
          <text class="metric-target">参考: 1500~2500 ml</text>
        </view>
      </view>
    </view>

    <view class="life-card">
      <view class="life-card-title">指标变化趋势</view>
      <view class="placeholder-content">
        <text class="tip-icon">📊</text>
        <text class="tip-text">健康指标录入与周/月走势图将在阶段 7 接入</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.health-page {
  padding-bottom: 30rpx;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.metric-card {
  background-color: #f7f8fa;
  border-radius: 12rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
}

.metric-name {
  font-size: 24rpx;
  color: #8c8c8c;
}

.metric-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2d3d;
  margin: 10rpx 0;
}

.metric-unit {
  font-size: 22rpx;
  font-weight: normal;
  color: #8c8c8c;
}

.metric-target {
  font-size: 20rpx;
  color: #52c41a;
}

.placeholder-content {
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c8c8c;
}

.tip-icon {
  font-size: 54rpx;
  margin-bottom: 12rpx;
}

.tip-text {
  font-size: 26rpx;
}
</style>

