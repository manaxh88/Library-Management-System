/**
 * 身份认证工具函数
 */

/**
 * 从 localStorage 获取 token
 */
export function getTokenFromLocalStorage(): string | null {
  return localStorage.getItem('token');
}

/**
 * 从 localStorage 获取用户 ID
 */
export function getUserIdFromLocalStorage(): string | null {
  return localStorage.getItem('userId');
}

/**
 * 从 token 中提取用户角色
 * token 格式：JWT，其中包含 role claim
 */
export function getRoleFromToken(token: string): number | null {
  try {
    const decoded = decodeJwtPayload(token);
    if (!decoded) {
      return null;
    }
    const role = decoded.role;
    return typeof role === 'number' ? role : role ? Number(role) : null;
  } catch (error) {
    console.error('Failed to parse token:', error);
    return null;
  }
}

/**
 * 从 token 中提取用户名
 */
export function getUsernameFromToken(token: string): string | null {
  try {
    const decoded = decodeJwtPayload(token);
    return decoded?.sub || null;
  } catch (error) {
    console.error('Failed to parse token:', error);
    return null;
  }
}

/**
 * 获取完整的用户信息
 */
export function getUserInfoFromToken(token: string) {
  try {
    return decodeJwtPayload(token);
  } catch (error) {
    console.error('Failed to parse token:', error);
    return null;
  }
}

function decodeJwtPayload(token: string): Record<string, any> | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }

  const payload = parts[1]
    .replace(/-/g, '+')
    .replace(/_/g, '/');
  const padded = payload.padEnd(payload.length + ((4 - (payload.length % 4)) % 4), '=');

  try {
    return JSON.parse(atob(padded));
  } catch (error) {
    console.error('Failed to decode token payload:', error);
    return null;
  }
}

/**
 * 检查用户是否拥有指定角色
 */
export function hasRole(token: string | null, ...roles: number[]): boolean {
  if (!token) {
    return false;
  }
  const userRole = getRoleFromToken(token);
  return userRole !== null && roles.includes(userRole);
}

/**
 * 检查用户是否是管理员
 */
export function isAdmin(token: string | null): boolean {
  return hasRole(token, 0);
}

/**
 * 检查用户是否是教师
 */
export function isTeacher(token: string | null): boolean {
  return hasRole(token, 1);
}

/**
 * 检查用户是否是学生
 */
export function isStudent(token: string | null): boolean {
  return hasRole(token, 2);
}

/**
 * 获取角色的中文名称
 */
export function getRoleName(role: number): string {
  const roleMap: { [key: number]: string } = {
    0: '管理员',
    1: '教师',
    2: '学生'
  };
  return roleMap[role] || '未知';
}

/**
 * 清除所有认证相关的数据
 */
export function clearAuthData(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
}

/**
 * 保存认证信息到 localStorage
 */
export function saveAuthInfo(token: string, userId: string, role: number, username?: string): void {
  localStorage.setItem('token', token);
  localStorage.setItem('userId', userId);
  localStorage.setItem('role', role.toString());
  if (username) {
    localStorage.setItem('username', username);
  }
}
