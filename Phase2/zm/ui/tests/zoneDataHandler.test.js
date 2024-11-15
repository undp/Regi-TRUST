const supertest = require('supertest')
const router = require('../src/server/router')
const express = require('express')
const nock = require('nock')
const mockData = require('./mockData/mockData')
require('dotenv').config()

describe('GET /api/zonedata', () => {
  it('should return zoneData', async ()=> {
    const zoneMockData = require('./mockData/mockData')
    const mockScope = nock(process.env.ZONEMGR_URL)
      .get('/view-zone')
      .reply(200, mockData)
    const server = await router(express())
    const response = await supertest(server).get('/ui/api/zonedata')
    console.log(response)
    expect(response.statusCode).toBe(200)
    expect(response.body).toEqual(mockData)
  })
})

describe('GET /api/zonedata', () => {
  it('should fail and return 500 error', async ()=> {
    const server = await router(express())
    const response = await supertest(server).get('/ui/api/zonedata')
    expect(response.statusCode).toBe(500)
  })
})