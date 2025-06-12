import storybook from 'eslint-plugin-storybook'

/**
 * .eslint.js
 *
 * ESLint configuration file.
 */

import pluginVue from 'eslint-plugin-vue'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import formatjs from 'eslint-plugin-formatjs'
import eslintConfigPrettier from 'eslint-config-prettier'

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  {
    name: 'app/files-to-ignore',
    ignores: [
      '**/dist/**',
      '**/dist-ssr/**',
      '**/coverage/**',
      'openapi-generator.mts',
      '**/generated/openapi/komga.d.ts',
      'public-msw/**/*',
      'eslint.config.ts',
    ],
  },

  ...pluginVue.configs['flat/essential'],
  vueTsConfigs.recommendedTypeChecked,

  {
    rules: {
      'prefer-promise-reject-errors': 'off',
      '@typescript-eslint/no-unused-expressions': [
        'error',
        {
          allowShortCircuit: true,
          allowTernary: true,
        },
      ],
      '@typescript-eslint/no-unused-vars': [
        'error',
        { caughtErrors: 'all', caughtErrorsIgnorePattern: '^ignore' },
      ],
      'no-empty': ['error', { allowEmptyCatch: true }],
      'vue/multi-word-component-names': 'off',
    },
  },

  formatjs.configs.recommended,

  {
    plugins: {
      formatjs,
    },
    rules: {
      'formatjs/enforce-id': [
        'error',
        {
          idInterpolationPattern: '[sha512:contenthash:base64:6]',
          idWhitelist: ['app.*'],
        },
      ],
    },
  },

  eslintConfigPrettier,

  ...storybook.configs['flat/recommended'],

  {
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
)
