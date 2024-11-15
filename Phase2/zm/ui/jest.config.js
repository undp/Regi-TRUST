/** @type {import('jest').Config} */
const config = {
  testTimeout: 2000,
  collectCoverage: true,
  ci: true,
  coverageReporters: [
    "cobertura",
    "lcov",
    "text"
  ],
  coveragePathIgnorePatterns: [
    "node_modules",
    "tests",
    "src/client"
  ]
};

module.exports = config;