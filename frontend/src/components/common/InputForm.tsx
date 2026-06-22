import "./InputForm.css"
import type { InputHTMLAttributes } from "react";

type InputFormProps = InputHTMLAttributes<HTMLInputElement>;

export function InputForm(props: InputFormProps) {
    return <input className="form-input" {...props} />;
}
