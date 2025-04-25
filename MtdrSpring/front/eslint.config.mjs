// @ts-check

import eslint from '@eslint/js';
import { globalIgnores } from 'eslint/config';
import tseslint from 'typescript-eslint';
import pluginReact from 'eslint-plugin-react';

export default tseslint.config([
  
  eslint.configs.recommended,
  tseslint.configs.recommended,
  pluginReact.configs.flat.recommended,
  pluginReact.configs.flat["jsx-runtime"],
  globalIgnores([ "dist/" ]),
  {
    ignores: ["dist/", "node_modules/", "selenium-tests/**/*.cjs"],
  },
  {
    settings: {
      react: {
        version: "detect",
      }
    }
  },
  
]);

