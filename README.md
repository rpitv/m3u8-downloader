# .m3u8 Video Downloader
Tool used for downloading m3u8 files into a single-file video format.

## Work in progress
This program will not yet work if you compile and try to run it. If you need something immediately, take a look at https://github.com/rpitv/m3u8-downloader/blob/master/rpitv-downloader-example.bat

## Usage
Initial theory for usage:
`java -jar downloader.jar input.m3u8 output.mp4`

HTTP inputs will also be accepted:
`java -jar downloader.jar http://example.com/livestream.m3u8 output.mp4`
