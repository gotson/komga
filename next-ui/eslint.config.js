/**
 * .eslint.js
 *
 * ESLint configuration file.
 */

import pluginVue from 'eslint-plugin-vue'
import {defineConfigWithVueTs, vueTsConfigs} from '@vue/eslint-config-typescript'
import formatjs from 'eslint-plugin-formatjs'

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/dist-ssr/**', '**/coverage/**', 'openapi-generator.mts'],
  },

  ...pluginVue.configs['flat/recommended'],
  vueTsConfigs.recommendedTypeChecked,

  {
    rules: {
      '@typescript-eslint/no-unused-expressions': [
        'error',
        {
          allowShortCircuit: true,
          allowTernary: true,
        },
      ],
      'vue/multi-word-component-names': 'off',
    }
  },

  {
    plugins: {
      formatjs,
    },
    rules: {
      'formatjs/no-offset': 'error',
      'formatjs/enforce-id': [
        'error',
        {
          idInterpolationPattern: '[sha512:contenthash:base64:6]',
        },
      ],
    },
  },
)
