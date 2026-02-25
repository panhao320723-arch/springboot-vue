import request from '../utils/request'

export function getAllUsers() {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

export function deleteUser(id) {
  return request({
    url: '/admin/users/' + id,
    method: 'delete'
  })
}

export function getAllItems() {
  return request({
    url: '/admin/items',
    method: 'get'
  })
}

export function deleteItem(id) {
  return request({
    url: '/admin/items/' + id,
    method: 'delete'
  })
}

export function updateItemStatus(id, status) {
  return request({
    url: `/admin/items/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function updateUserRole(userId, role) {
  return request({
    url: `/admin/users/${userId}/role`,
    method: 'put',
    params: { role }
  })
}

export function silenceUser(userId, days) {
  return request({
    url: `/admin/users/${userId}/silence`,
    method: 'put',
    params: { days }
  })
}

export function getAnnouncements() {
  return request({
    url: '/announcements/public/list',
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}
