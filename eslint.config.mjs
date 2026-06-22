// @ts-check
import tsPlugin from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import angularPlugin from '@angular-eslint/eslint-plugin';
import angularTemplatePlugin from '@angular-eslint/eslint-plugin-template';
import angularTemplateParser from '@angular-eslint/template-parser';

const tsRecommended = tsPlugin.configs['flat/recommended'];

export default [
    {ignores: ['src/main/webapp/app/rest.ts']},
    // TypeScript files
    ...tsRecommended,
    {
        files: ['src/main/webapp/**/*.ts'],
        languageOptions: {
            parser: tsParser,
            parserOptions: {
                project: ['./tsconfig.json', './tsconfig.app.json'],
                createDefaultProgram: true,
            },
        },
        plugins: {
            '@angular-eslint': angularPlugin,
        },
        processor: angularTemplatePlugin.processors['extract-inline-html'],
        rules: {
            '@angular-eslint/contextual-lifecycle': 'error',
            '@angular-eslint/no-empty-lifecycle-method': 'error',
            '@angular-eslint/no-input-rename': 'error',
            '@angular-eslint/no-inputs-metadata-property': 'error',
            '@angular-eslint/no-output-native': 'error',
            '@angular-eslint/no-output-on-prefix': 'error',
            '@angular-eslint/no-output-rename': 'error',
            '@angular-eslint/no-outputs-metadata-property': 'error',
            '@angular-eslint/prefer-inject': 'error',
            '@angular-eslint/prefer-on-push-component-change-detection': 'off',
            '@angular-eslint/use-pipe-transform-interface': 'error',
            '@angular-eslint/prefer-standalone': 'off',
            '@angular-eslint/directive-selector': [
                'error',
                {type: 'attribute', prefix: 'app', style: 'camelCase'},
            ],
            '@angular-eslint/component-selector': [
                'error',
                {type: 'element', prefix: 'app', style: 'kebab-case'},
            ],
        },
    },
    // HTML template files
    {
        files: ['src/main/webapp/**/*.html'],
        languageOptions: {
            parser: angularTemplateParser,
        },
        plugins: {
            '@angular-eslint/template': angularTemplatePlugin,
        },
        rules: {
            '@angular-eslint/template/banana-in-box': 'error',
            '@angular-eslint/template/eqeqeq': 'error',
            '@angular-eslint/template/no-negated-async': 'error',
            '@angular-eslint/template/prefer-control-flow': 'error',
        },
    },
];
