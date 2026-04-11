export const REFRESH_TOKEN_KEY = 'refreshToken'
export const CHECKOUT_REFRESH_BRIDGE_KEY = 'checkout-refresh-token'

export function persistCheckoutRefreshToken() {
  const refreshToken =
    localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY)
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(CHECKOUT_REFRESH_BRIDGE_KEY, refreshToken)
  }
}

export function restoreCheckoutRefreshToken() {
  const currentToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  if (currentToken) {
    return currentToken
  }

  const bridgedRefreshToken = localStorage.getItem(CHECKOUT_REFRESH_BRIDGE_KEY)
  if (bridgedRefreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, bridgedRefreshToken)
    return bridgedRefreshToken
  }

  return null
}

export function clearCheckoutRefreshToken() {
  localStorage.removeItem(CHECKOUT_REFRESH_BRIDGE_KEY)
}
