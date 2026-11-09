"""Rebuild local Android icons (optional: Python 3 + Pillow)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
root=Path(__file__).resolve().parents[1]/'android/app/src/main/res'
# Geometric vector-like monogram, no font dependency.
def icon(size,transparent=False):
    im=Image.new('RGBA',(size,size),(0,0,0,0) if transparent else '#17624f');d=ImageDraw.Draw(im);k=size/108
    d.rounded_rectangle((35*k,23*k,47*k,76*k),radius=4*k,fill='#ffffff')
    d.ellipse((35*k,42*k,78*k,85*k),fill='#ffffff');d.ellipse((47*k,54*k,66*k,73*k),fill='#17624f')
    return im
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    folder=root/('mipmap-'+density);folder.mkdir(exist_ok=True)
    icon(size).save(folder/'ic_launcher.png');icon(size).save(folder/'ic_launcher_round.png');icon(size*3//2,True).save(folder/'ic_launcher_foreground.png')
for p in root.glob('drawable*/splash.png'):p.unlink()
