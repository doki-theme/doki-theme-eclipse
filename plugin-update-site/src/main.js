let previousListener;

function drawBackground() {
  const backgroundCanvas = document.getElementById(
    "backgroundImage"
  );
  // @ts-ignore
  const ctx = backgroundCanvas.getContext("2d");
  if (!ctx) return;
  const mainCanvas = document.getElementById("main");
  if (!mainCanvas) {
    return;
  }

  const boundingRect = mainCanvas.getBoundingClientRect();
  const w = boundingRect.width;
  const h = boundingRect.height;

  backgroundCanvas.setAttribute('width', String(w));
  backgroundCanvas.setAttribute('height', String(h))


  ctx.clearRect(0, 0, w, h);
  ctx.beginPath();
  ctx.moveTo(0, h * 0.85);
  ctx.quadraticCurveTo(w / 1.85, h, w, 0);
  ctx.lineTo(w, h);
  ctx.lineTo(0, h);

  const color = '#282b42'
  ctx.fillStyle = color;
  ctx.strokeStyle = color;
  ctx.fill();
  ctx.closePath();
  ctx.stroke();
}

document.addEventListener("DOMContentLoaded", () => {
    drawBackground();
  const listener = () => {
    drawBackground();
  };
  previousListener = listener;
  window.addEventListener('resize', listener);
})
