const ionic = require('@ionic/eslint-config/recommended');

module.exports = [
  {
    ignores: [
      'build/**',
      'dist/**',
      'node_modules/**',
      'eslint.config.cjs',
    ],
  },
  ...ionic,
];
