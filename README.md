Usage: ilx2m3u <iTunesMusicLibraryXml> <outputPath>
1. Parsing <> iTunes music library xml file.
2. Output playlists in m3u format to <outputPath>
3. Copy all the tracks within playlists to <outputPath>


samplePlaylist.m3u --
#EXTM3U
sampleArtist1/sampleAlbum1/sampleTrack1.mp3
sampleArtist1/sampleAlbum1/sampleTrack2.mp3
sampleArtist1/sampleAlbum2/sampleTrack3.mp3
sampleArtist2/sampleAlbum3/sampleTrack4.mp3


<outputPath>
 │      samplePlaylist.m3u
 │
 ├─ sampleArtist1
 │   │
 │   ├─ sampleAlbum1
 │   │      sampleTrack1.mp3
 │   │      sampleTrack2.mp3
 │   │
 │   └─ sampleAlbum2
 │          sampleTrack3.mp3
 │
 └─ sampleArtist2
     │
     └─ sampleAlbum3
            sampleTrack4.mp3

