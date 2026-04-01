import api from './axios'; // 방금 만드신 axios 인스턴스 임포트

export const noticeApi = {
  // [FN-001] 사용자 공지사항 목록 조회 (페이징) [cite: 1102]
  getNoticeList(page = 0) {
    return api.get(`/notice/list`, { params: { page } });
  },

  // [FN-002] 사용자 공지사항 상세 조회 [cite: 1103]
  getNoticeDetail(id) {
    return api.get(`/notice/detail/${id}`);
  },

  // [FA-024] 관리자 공지사항 목록 조회 [cite: 929]
  getAdminNoticeList(page = 0) {
    return api.get(`/admin/notice/list`, { params: { page } });
  },

  // [FA-026] 관리자 공지사항 등록 
  createNotice(data) {
    return api.post(`/admin/notice/insert`, data);
  },

  // [FA-028] 관리자 공지사항 삭제 (Soft Delete) [cite: 936]
  deleteNotice(id) {
    return api.delete(`/admin/notice/${id}`);
  }
};