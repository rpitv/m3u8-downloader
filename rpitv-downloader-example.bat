@echo off
set /p link=Enter .m3u8 URL:
set /p filename=Enter output file name: 
/ffmpeg/bin/ffmpeg -i %link% -bsf:a aac_adtstoasc -vcodec copy -c copy -crf 50 %filename%
