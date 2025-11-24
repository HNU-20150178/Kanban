<template>
  <div class="kanban-board">
    <h1>칸반 보드</h1>
    
    <div class="board-header">
      <button @click="showCreateModal = true" class="btn-create">새 업무 추가</button>
    </div>

    <div class="board-columns">
      <div v-for="status in statuses" :key="status.value" class="column">
        <div class="column-header">
          <h2>{{ status.label }}</h2>
          <span class="count">{{ getTasksByStatus(status.value).length }}</span>
        </div>
        
        <draggable 
          :list="getTasksByStatus(status.value)"
          :group="{ name: 'tasks' }"
          @end="onDragEnd"
          class="task-list"
          item-key="id"
        >
          <template #item="{ element }">
            <div class="task-card" :class="'priority-' + (element.priority || 'MEDIUM').toLowerCase()">
              <div class="task-header">
                <h3>{{ element.title }}</h3>
                <div class="task-actions">
                  <button @click="editTask(element)" class="btn-icon">수정</button>
                  <button @click="deleteTaskConfirm(element)" class="btn-icon btn-delete">삭제</button>
                </div>
              </div>
              
              <p class="task-description">{{ element.description }}</p>
              
              <div class="task-footer">
                <span v-if="element.assignee" class="assignee">{{ element.assignee }}</span>
                <span class="priority-badge">{{ getPriorityLabel(element.priority) }}</span>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>

    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h2>{{ showEditModal ? '업무 수정' : '새 업무 만들기' }}</h2>
        
        <form @submit.prevent="saveTask">
          <div class="form-group">
            <label>제목</label>
            <input v-model="taskForm.title" required class="form-input" placeholder="업무 제목을 입력하세요" />
          </div>
          
          <div class="form-group">
            <label>설명</label>
            <textarea v-model="taskForm.description" rows="4" class="form-input" placeholder="업무 설명을 입력하세요"></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>상태</label>
              <select v-model="taskForm.status" required class="form-input">
                <option v-for="status in statuses" :key="status.value" :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>우선순위</label>
              <select v-model="taskForm.priority" class="form-input">
                <option value="LOW">낮음</option>
                <option value="MEDIUM">보통</option>
                <option value="HIGH">높음</option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label>담당자</label>
            <input v-model="taskForm.assignee" class="form-input" placeholder="담당자 이름" />
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn-cancel">취소</button>
            <button type="submit" class="btn-save">저장</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import api from '../services/api.js';

export default {
  name: 'KanbanBoard',
  components: {
    draggable
  },
  data() {
    return {
      tasks: [],
      statuses: [
        { value: 'TODO', label: 'TODO' },
        { value: 'IN_PROGRESS', label: 'In Progress' },
        { value: 'DONE', label: 'Done' }
      ],
      showCreateModal: false,
      showEditModal: false,
      taskForm: {
        title: '',
        description: '',
        status: 'TODO',
        priority: 'MEDIUM',
        assignee: '',
        position: 0
      },
      editingTaskId: null
    };
  },
  mounted() {
    this.loadTasks();
  },
  methods: {
    async loadTasks() {
      try {
        const response = await api.getAllTasks();
        this.tasks = response.data;
      } catch (error) {
        console.error('업무 로드 실패:', error);
        alert('업무를 불러오는데 실패했습니다.');
      }
    },
    
    getTasksByStatus(status) {
      return this.tasks.filter(task => task.status === status);
    },
    
    async onDragEnd(event) {
      const movedTask = this.tasks.find(t => t.id === event.item.__draggable_context.element.id);
      if (!movedTask) return;
      
      const columnElement = event.to.closest('.column');
      const statusHeader = columnElement.querySelector('.column-header h2').textContent.trim();
      const newStatus = this.statuses.find(s => s.label === statusHeader)?.value;
      
      try {
        await api.moveTask(movedTask.id, newStatus, event.newIndex);
        await this.loadTasks();
      } catch (error) {
        console.error('업무 이동 실패:', error);
        alert('업무 이동에 실패했습니다.');
        await this.loadTasks();
      }
    },
    
    editTask(task) {
      this.editingTaskId = task.id;
      this.taskForm = {
        title: task.title,
        description: task.description,
        status: task.status,
        priority: task.priority || 'MEDIUM',
        assignee: task.assignee || '',
        position: task.position
      };
      this.showEditModal = true;
    },
    
    async deleteTaskConfirm(task) {
      if (confirm(`"${task.title}" 업무를 삭제하시겠습니까?`)) {
        try {
          await api.deleteTask(task.id);
          await this.loadTasks();
        } catch (error) {
          console.error('업무 삭제 실패:', error);
          alert('업무 삭제에 실패했습니다.');
        }
      }
    },
    
    async saveTask() {
      try {
        if (this.showEditModal) {
          await api.updateTask(this.editingTaskId, this.taskForm);
        } else {
          await api.createTask(this.taskForm);
        }
        await this.loadTasks();
        this.closeModal();
      } catch (error) {
        console.error('업무 저장 실패:', error);
        alert('업무 저장에 실패했습니다.');
      }
    },
    
    closeModal() {
      this.showCreateModal = false;
      this.showEditModal = false;
      this.editingTaskId = null;
      this.taskForm = {
        title: '',
        description: '',
        status: 'TODO',
        priority: 'MEDIUM',
        assignee: '',
        position: 0
      };
    },
    
    getPriorityLabel(priority) {
      const labels = {
        'LOW': '낮음',
        'MEDIUM': '보통',
        'HIGH': '높음'
      };
      return labels[priority] || '보통';
    }
  }
};
</script>

<style scoped>
.kanban-board {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

h1 {
  margin-bottom: 20px;
  color: #333;
}

.board-header {
  margin-bottom: 20px;
}

.btn-create {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-create:hover {
  background-color: #45a049;
}

.board-columns {
  display: flex;
  gap: 20px;
  overflow-x: auto;
}

.column {
  flex: 1;
  min-width: 300px;
  background-color: #e8e8e8;
  border-radius: 8px;
  padding: 15px;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ccc;
}

.column-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.count {
  background-color: #666;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.task-list {
  min-height: 200px;
}

.task-card {
  background-color: white;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: move;
  border-left: 4px solid #ddd;
}

.task-card.priority-high {
  border-left-color: #f44336;
}

.task-card.priority-medium {
  border-left-color: #ff9800;
}

.task-card.priority-low {
  border-left-color: #4CAF50;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.task-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  flex: 1;
}

.task-actions {
  display: flex;
  gap: 5px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
  border-radius: 3px;
}

.btn-icon:hover {
  background-color: #f0f0f0;
}

.btn-delete:hover {
  background-color: #ffebee;
  color: #f44336;
}

.task-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.4;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.assignee {
  color: #666;
  background-color: #f5f5f5;
  padding: 3px 8px;
  border-radius: 3px;
}

.priority-badge {
  padding: 3px 8px;
  border-radius: 3px;
  font-weight: 500;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #4CAF50;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel,
.btn-save {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #333;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-save {
  background-color: #4CAF50;
  color: white;
}

.btn-save:hover {
  background-color: #45a049;
}
</style>