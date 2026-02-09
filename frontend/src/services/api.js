import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

export default {
  // 모든 업무 조회
  getAllTasks() {
    return api.get('/tasks');
  },

  // 상태별 업무 조회
  getTasksByStatus(status) {
    return api.get(`/tasks/status/${status}`);
  },

  // 업무 생성
  createTask(task) {
    return api.post('/tasks', task);
  },

  // 업무 수정
  updateTask(id, task) {
    return api.put(`/tasks/${id}`, task);
  },

  // 업무 이동
  moveTask(id, status, position) {
    return api.patch(`/tasks/${id}/move`, { status, position });
  },

  // 업무 삭제
  deleteTask(id) {
    return api.delete(`/tasks/${id}`);
  }
};