update media
set STATUS = 'OUTDATED'
where MEDIA_TYPE = 'application/pdf'
  and STATUS = 'READY';
