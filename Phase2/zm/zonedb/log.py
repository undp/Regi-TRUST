"""
Init the logging
"""
import logging.config
from os import getenv

# create logger
logger = logging.getLogger()
logger.setLevel(logging.DEBUG)

# create console handler and set level to debug
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)

# create file handler and set level to debug
fh = logging.FileHandler(getenv('ZM_PATH', '/usr/lib/zonemgr') + '/logger.log')
fh.setLevel(logging.DEBUG)

# create formatter
formatter = logging.Formatter('[%(levelname)s] - %(asctime)s - %(name)s - %(message)s')

# add formatter to ch and fh
ch.setFormatter(formatter)
fh.setFormatter(formatter)

# add ch to logger
logger.addHandler(ch)
logger.addHandler(fh)

# 'application' code
# uncomment to test log if need
# logger.debug('debug message')
# logger.info('info message')
# logger.warning('warn message')
# logger.error('error message')
# logger.critical('critical message')
