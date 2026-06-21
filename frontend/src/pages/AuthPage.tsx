import drawing from "../assets/ui/drawing.png";
export default function AuthPage() {
  return (
    <div>
      <div style={{ textAlign: "center", padding: "16px" }}>
        <img src={drawing} alt="drawing" style={{ height: "200px" }} />
        <h1>Сторінка для авторизації</h1>
      </div>
    </div>
  );
}