<template>
  <transition name="toast-fade">
    <div v-if="show" :class="['toast', `toast-${type}`]">
      <div class="toast-content">
        <span class="toast-icon">{{ icon }}</span>
        <span class="toast-message">{{ message }}</span>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'AppToast',
  data() {
    return {
      show: false,
      message: '',
      type: 'info',
      duration: 3000,
      timer: null
    };
  },
  computed: {
    icon() {
      const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
      };
      return icons[this.type] || icons.info;
    }
  },
  methods: {
    showToast(message, type = 'info', duration = 3000) {
      this.message = message;
      this.type = type;
      this.duration = duration;
      this.show = true;

      if (this.timer) {
        clearTimeout(this.timer);
      }

      this.timer = setTimeout(() => {
        this.show = false;
      }, this.duration);
    },
    success(message, duration = 3000) {
      this.showToast(message, 'success', duration);
    },
    error(message, duration = 4000) {
      this.showToast(message, 'error', duration);
    },
    warning(message, duration = 3500) {
      this.showToast(message, 'warning', duration);
    },
    info(message, duration = 3000) {
      this.showToast(message, 'info', duration);
    }
  }
};
</script>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 300px;
  max-width: 500px;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  font-size: 14px;
  font-weight: 500;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toast-icon {
  font-size: 20px;
  font-weight: bold;
  flex-shrink: 0;
}

.toast-message {
  flex: 1;
  line-height: 1.5;
}

.toast-success {
  background-color: #10b981;
  color: white;
}

.toast-error {
  background-color: #ef4444;
  color: white;
}

.toast-warning {
  background-color: #f59e0b;
  color: white;
}

.toast-info {
  background-color: #3b82f6;
  color: white;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>