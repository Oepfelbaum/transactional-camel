const express = require('express')
const app = express()
const bodyParser = require('body-parser')
const port = 3000

app.use(bodyParser.text( {extended: true }))

app.post('/api/messageSink', (req, res) => {
  if (req.body.includes("Error")) {
    console.log('Message received, but its faulty!')
    res.status(400).send()
  } else {
    console.log('Message successfully consumed')
    res.status(200).send()
  }
})

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})