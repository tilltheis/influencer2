import { InputHTMLAttributes, TextareaHTMLAttributes, useMemo, useState } from "react";
import classNames from "./classNames";
import "./Input.css";

export type BaseInputProps = {
  label: string;
  multiline?: boolean;
};
export type InputProps = BaseInputProps &
  InputHTMLAttributes<HTMLInputElement> & {
    multiline?: false;
  };
export type MultilineInputProps = BaseInputProps &
  TextareaHTMLAttributes<HTMLTextAreaElement> & {
    multiline: true;
  };

export default function Input(props: InputProps | MultilineInputProps) {
  const [hasFocus, setHasFocus] = useState(false);

  const {
    label: propsLabel,
    placeholder: propsPlaceholder,
    required,
    multiline,
    className,
    onFocus,
    onBlur,
    ...elementSpecificProps
  } = props;

  const placeholder = hasFocus ? propsPlaceholder : " ";
  const label = required ? (
    <>
      {propsLabel} <small>(required)</small>
    </>
  ) : (
    propsLabel
  );

  const sharedProps = {
    className: classNames("Input__Input", className),
    placeholder,
    required,
    onFocus: (e: any) => {
      setHasFocus(true);
      onFocus && onFocus(e);
    },
    onBlur: (e: any) => {
      setHasFocus(false);
      onBlur && onBlur(e);
    },
  };

  const input = multiline ? (
    <textarea
      {...(elementSpecificProps as TextareaHTMLAttributes<HTMLTextAreaElement>)}
      {...sharedProps}
    />
  ) : (
    <input {...(elementSpecificProps as InputHTMLAttributes<HTMLInputElement>)} {...sharedProps} />
  );

  return (
    <label className={classNames("Input", multiline && "Input--multiline")}>
      <div className="Input__Label">{label}</div>
      {input}
    </label>
  );
}
