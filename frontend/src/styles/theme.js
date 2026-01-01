/**
 * Modern UI Theme
 * Centralized color scheme and styling constants
 */

export const theme = {
  colors: {
    primary: '#2563eb',
    primaryDark: '#1e40af',
    primaryLight: '#3b82f6',
    secondary: '#10b981',
    secondaryDark: '#059669',
    danger: '#ef4444',
    dangerDark: '#dc2626',
    warning: '#f59e0b',
    success: '#10b981',
    info: '#3b82f6',
    
    // Grays
    gray50: '#f9fafb',
    gray100: '#f3f4f6',
    gray200: '#e5e7eb',
    gray300: '#d1d5db',
    gray400: '#9ca3af',
    gray500: '#6b7280',
    gray600: '#4b5563',
    gray700: '#374151',
    gray800: '#1f2937',
    gray900: '#111827',
    
    // Backgrounds
    bgPrimary: '#ffffff',
    bgSecondary: '#f9fafb',
    bgTertiary: '#f3f4f6',
    
    // Text
    textPrimary: '#111827',
    textSecondary: '#4b5563',
    textTertiary: '#6b7280',
    textInverse: '#ffffff',
    
    // Borders
    borderLight: '#e5e7eb',
    borderMedium: '#d1d5db',
    borderDark: '#9ca3af',
  },
  
  spacing: {
    xs: '0.25rem',    // 4px
    sm: '0.5rem',     // 8px
    md: '1rem',       // 16px
    lg: '1.5rem',     // 24px
    xl: '2rem',       // 32px
    '2xl': '3rem',    // 48px
    '3xl': '4rem',    // 64px
  },
  
  borderRadius: {
    sm: '0.375rem',   // 6px
    md: '0.5rem',     // 8px
    lg: '0.75rem',    // 12px
    xl: '1rem',       // 16px
    full: '9999px',
  },
  
  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
  },
  
  transitions: {
    fast: '150ms ease-in-out',
    normal: '300ms ease-in-out',
    slow: '500ms ease-in-out',
  },
  
  breakpoints: {
    sm: '640px',
    md: '768px',
    lg: '1024px',
    xl: '1280px',
  },
};

export const getCardStyle = (hover = true) => ({
  backgroundColor: theme.colors.bgPrimary,
  borderRadius: theme.borderRadius.lg,
  padding: theme.spacing.xl,
  boxShadow: theme.shadows.md,
  border: `1px solid ${theme.colors.borderLight}`,
  transition: hover ? `all ${theme.transitions.normal}` : 'none',
  ...(hover && {
    '&:hover': {
      boxShadow: theme.shadows.lg,
      transform: 'translateY(-2px)',
    },
  }),
});

export const getButtonStyle = (variant = 'primary', size = 'md') => {
  const baseStyle = {
    padding: size === 'sm' ? `${theme.spacing.sm} ${theme.spacing.md}` : 
             size === 'lg' ? `${theme.spacing.md} ${theme.spacing.xl}` :
             `${theme.spacing.sm} ${theme.spacing.lg}`,
    borderRadius: theme.borderRadius.md,
    border: 'none',
    fontWeight: '600',
    fontSize: size === 'sm' ? '0.875rem' : size === 'lg' ? '1.125rem' : '1rem',
    cursor: 'pointer',
    transition: `all ${theme.transitions.fast}`,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: theme.spacing.sm,
  };
  
  const variants = {
    primary: {
      backgroundColor: theme.colors.primary,
      color: theme.colors.textInverse,
      '&:hover': {
        backgroundColor: theme.colors.primaryDark,
      },
      '&:disabled': {
        backgroundColor: theme.colors.gray400,
        cursor: 'not-allowed',
      },
    },
    secondary: {
      backgroundColor: theme.colors.secondary,
      color: theme.colors.textInverse,
      '&:hover': {
        backgroundColor: theme.colors.secondaryDark,
      },
    },
    danger: {
      backgroundColor: theme.colors.danger,
      color: theme.colors.textInverse,
      '&:hover': {
        backgroundColor: theme.colors.dangerDark,
      },
    },
    outline: {
      backgroundColor: 'transparent',
      color: theme.colors.primary,
      border: `2px solid ${theme.colors.primary}`,
      '&:hover': {
        backgroundColor: theme.colors.primary,
        color: theme.colors.textInverse,
      },
    },
    ghost: {
      backgroundColor: 'transparent',
      color: theme.colors.textSecondary,
      '&:hover': {
        backgroundColor: theme.colors.gray100,
      },
    },
  };
  
  return { ...baseStyle, ...variants[variant] };
};

export const getInputStyle = () => ({
  width: '100%',
  padding: `${theme.spacing.md} ${theme.spacing.lg}`,
  border: `1px solid ${theme.colors.borderMedium}`,
  borderRadius: theme.borderRadius.md,
  fontSize: '1rem',
  transition: `all ${theme.transitions.fast}`,
  '&:focus': {
    outline: 'none',
    borderColor: theme.colors.primary,
    boxShadow: `0 0 0 3px ${theme.colors.primary}20`,
  },
  '&:disabled': {
    backgroundColor: theme.colors.gray100,
    cursor: 'not-allowed',
  },
});

