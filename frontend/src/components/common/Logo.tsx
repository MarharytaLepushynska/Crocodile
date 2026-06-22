import "./Logo.css"
import logo from "../../assets/ui/logo.png";

interface LogoProps {
    size?: "large" | "medium";
}

export function Logo({ size = "large" }: LogoProps) {
    return (
        <img
            src={logo}
            alt="Crocodile"
            className={`logo logo--${size}`}
        />
    );
}
