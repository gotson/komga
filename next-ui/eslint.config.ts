/**
 * .eslint.js
 *
 * ESLint configuration file.
 */

import pluginVue from 'eslint-plugin-vue'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import formatjs from 'eslint-plugin-formatjs'
import eslintConfigPrettier from 'eslint-config-prettier'
import storybook from 'eslint-plugin-storybook'

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
      '**/storybook-static/**',
      'openapi-generator.mts',
      '**/generated/openapi/komga.d.ts',
      'public-msw/**/*',
      'eslint.config.ts',
    ],
  },

  ...pluginVue.configs['flat/recommended'],
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
      'vue/require-default-prop': 'off',
      'vue/component-name-in-template-casing': [
        'error',
        'PascalCase',
        {
          registeredComponentsOnly: false,
          ignores: ['/^v-/', 'draggable'],
        },
      ],
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
          idWhitelist: ['app.*', 'enum.*'],
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
