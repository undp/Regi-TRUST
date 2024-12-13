'use strict';
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var debug = require('debug')('http')
var logger = require('morgan');
var expressSession = require('express-session');
var bodyParser = require('body-parser');
var crypto = require("crypto");

var routes = require('./routes/index');
var users = require('./routes/users');
var auth = require('./routes/auth/auth');
var networkSubmissionForm = require('./routes/forms/NetworkEntrySubmission');
var reviewSubmissions = require('./routes/reviewSubmissions/reviewSubmissions');
var networkEntries = require('./routes/networkEntries/networkEntries');
var framework = require('./routes/framework/framework');
var frameworkForm = require('./routes/forms/Framework');
var enrollForm = require('./routes/forms/Enroll');
var reviewEnrollmentRequests = require('./routes/reviewSubmissions/reviewEnrollmentRequests');
const { getRoles } = require('./Auth/keycloak');

var app = express();

// maps the unique ID of a network entry submission to the XML string representing that submission
global.Submissions = {}

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

require('./data/MongoDB/mongoose').initMongo()

app.use(expressSession({
    genid: () => crypto.randomBytes(16).toString("hex"),
    secret: "thisismysecrctekeyfhrgfgrfrty84fwir767",
    saveUninitialized: true,
    cookie: { maxAge: 1000 * 60 * 30 }, // 30 minutes
    resave: false,
    store: require('./data/MongoDB/session')
}));

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);
app.use('/auth', auth)
app.use('/users', users);
app.use('/forms/gccn-network-entry-submission', networkSubmissionForm);
app.use('/review-submissions', reviewSubmissions);
app.use('/network-entries', networkEntries);
app.use('/enroll', enrollForm);
app.use('/review-enrollment-requests', reviewEnrollmentRequests);
app.use('/forms/gccn-framework', frameworkForm);
app.use('/framework', framework);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function (err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.status + ': ' + err.message,
            error: err,
            roles: getRoles(req),
            title: 'Error: ' + err.status
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function (err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.status + ': ' + err.message,
        error: {},
        roles: getRoles(req),
        title: 'Error: ' + err.status
    });
});

app.set('port', process.env.PORT || 3000);

var server = app.listen(app.get('port'), function () {
    debug('Express server listening on port ' + server.address().port);
});
