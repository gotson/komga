<template>
  <div :class="type">
    <q-banner
      rounded
      class="k-banner"
    >
      <div class="row items-center">
        <q-icon
          :name="icon"
          class="k-banner-icon"
          size="md"
          left
        />
        <span class="text-subtitle1 k-banner-text">
          <slot />
        </span>
      </div>
    </q-banner>
  </div>
</template>

<script setup lang="ts">
interface Props {
  type?: 'positive' | 'warning'
  tonal?: boolean
}

const { type } = defineProps<Props>()

const icon = computed(() => {
  switch (type) {
    case 'positive':
      return 'mdi-check-circle'
    case 'warning':
      return 'mdi-alert-circle'
  }
})
</script>

<script lang="ts"></script>

<style scoped lang="scss">
@use 'quasar/src/css/variables' as q;

.body--light {
  .positive {
    .k-banner {
      background: q.$green-1;
    }
    .k-banner-icon {
      color: q.$green;
    }
    .k-banner-text {
      color: q.$green;
    }
  }
  .warning {
    .k-banner {
      background: q.$orange-1;
    }
    .k-banner-icon {
      color: q.$orange;
    }
    .k-banner-text {
      color: q.$orange;
    }
  }
}

.body--dark {
  .positive {
    .k-banner {
      background: #1a291b;
    }
    .k-banner-icon {
      color: q.$green;
    }
    .k-banner-text {
      color: q.$green;
    }
  }
  .warning {
    .k-banner {
      background: #372413;
    }
    .k-banner-icon {
      color: q.$orange;
    }
    .k-banner-text {
      color: q.$orange;
    }
  }
}
</style>
