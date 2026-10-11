<template>
  <div class="action-buttons" v-if="mode === 'inline'">
    <div class="button-row">
      <el-button
        v-for="(action, idx) in visibleActions"
        :key="idx"
        :type="action.type || 'primary'"
        plain
        size="small"
        @click="handleAction(action)"
      >
        {{ getLabel(action) }}
      </el-button>
    </div>
  </div>
  <div class="action-buttons" v-else>
    <el-button
      v-if="visibleActions.length > 0"
      :type="visibleActions[0].type || 'primary'"
      plain
      size="small"
      @click="handleAction(visibleActions[0])"
    >
      {{ getLabel(visibleActions[0]) }}
    </el-button>
    <el-dropdown v-if="visibleActions.length > 1" trigger="click" @command="handleDropdownCommand">
      <el-button size="small" plain>
        更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="(action, idx) in visibleActions.slice(1)"
            :key="idx"
            :command="idx + 1"
            :divided="action.type === 'danger'"
          >
            {{ getLabel(action) }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  actions: {
    type: Array,
    required: true
    // [{ label, type, onClick, confirm, confirmMessage, condition }]
  },
  mode: {
    type: String,
    default: 'inline' // 'inline' | 'dropdown'
  },
  row: {
    type: Object,
    default: null
  }
})

const visibleActions = computed(() => {
  return props.actions.filter(action => {
    if (typeof action.condition === 'function') {
      return action.condition(props.row)
    }
    return true
  })
})

function getLabel(action) {
  if (typeof action.label === 'function') {
    return action.label(props.row)
  }
  return action.label
}

async function handleAction(action) {
  if (action.confirm) {
    const msg = typeof action.confirmMessage === 'function'
      ? action.confirmMessage(props.row)
      : action.confirmMessage || '确认执行此操作？'
    await ElMessageBox.confirm(msg, '操作确认', { type: 'warning' })
  }
  action.onClick(props.row)
}

function handleDropdownCommand(idx) {
  handleAction(visibleActions.value[idx])
}
</script>
