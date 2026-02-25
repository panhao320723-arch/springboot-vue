import request from '../utils/request'

export function getPublicItems(params) {
  return request({
    url: '/items/public/list',
    method: 'get',
    params
  })
}

export function publishItem(data) {
  return request({
    url: '/items',
    method: 'post',
    data
  })
}

export function getMyItems() {
  return request({
    url: '/items/my',
    method: 'get'
  })
}

export function markAsSolved(id) {
  return request({
    url: `/items/${id}/solve`,
    method: 'put'
  })
}

export function deleteMyItem(id) {
  return request({
    url: `/items/${id}`,
    method: 'delete'
  })
}

export function updateItem(id, data) {
  return request({
    url: `/items/${id}`,
    method: 'put',
    data
  })
}
